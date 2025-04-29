package ru.danl.kgram.states

import ru.danl.kgram.KGram
import ru.danl.kgram.kGram
import ru.danl.kgram.states.store.HashMapStateStore
import ru.danl.kgram.states.store.StateStore
import kotlin.reflect.KClass

/**
 * Configures state management for a [KGram] instance.
 *
 * @param State The type of the current state.
 * @param GlobalState The type of the global state.
 * @param stateStore The [StateStore] to use for state persistence. Defaults to [HashMapStateStore].
 * @param configure A lambda to configure the [States] instance.
 * @return A configured [States] instance.
 */
fun <State : Any, GlobalState : Any> KGram.states(
    stateStore: StateStore<State, GlobalState> = HashMapStateStore(),
    configure: States<State, GlobalState>.() -> Unit
) = States(stateStore, this).apply(configure)

/**
 * A class for managing user states in a Telegram bot.
 *
 * @param State The type of the current state.
 * @param GlobalState The type of the global state.
 * @param stateStore The [StateStore] used for state persistence.
 * @param kGram The [KGram] instance associated with this state manager.
 */
class States<State : Any, GlobalState : Any> internal constructor(
    private val stateStore: StateStore<State, GlobalState>,
    kGram: KGram
) {
    private val stateHandlers = mutableMapOf<KClass<out State>, StateHandler<State, State, GlobalState>>()

    init {
        kGram.handleMessage { message ->
            val userId = message.from.id
            withStateHandler(userId, this) { it.handleMessage(message) }
        }
        kGram.handleEditedMessage { message ->
            val userId = message.from.id
            withStateHandler(userId, this) { it.handleEditedMessage(message) }
        }
        kGram.handleCallbackQuery { callbackQuery ->
            val userId = callbackQuery.from.id
            withStateHandler(userId, this) { it.handleCallbackQuery(callbackQuery) }
        }
    }

    /**
     * Retrieves the state for a user.
     *
     * @param userId The ID of the user.
     * @return The [StatesHolder] containing the user's current and global state, or null if not found.
     */
    suspend fun get(userId: Long) = stateStore.get(userId)

    /**
     * Sets the current state for a user.
     *
     * @param userId The ID of the user.
     * @param currentState The new current state.
     */
    suspend fun setCurrentState(userId: Long, currentState: State) {
        set(
            userId = userId,
            kGram = kGram(),
            statesHolder = stateStore.get(userId)?.copy(current = currentState) ?: StatesHolder(current = currentState)
        )
    }

    /**
     * Sets the global state for a user.
     *
     * @param userId The ID of the user.
     * @param globalState The new global state.
     */
    suspend fun setGlobalState(userId: Long, globalState: GlobalState) {
        set(
            userId = userId,
            kGram = kGram(),
            statesHolder = stateStore.get(userId)?.copy(global = globalState) ?: StatesHolder(global = globalState)
        )
    }

    /**
     * Sets both the current and global state for a user.
     *
     * @param userId The ID of the user.
     * @param currentState The new current state.
     * @param globalState The new global state.
     */
    suspend fun set(userId: Long, currentState: State, globalState: GlobalState) {
        set(
            userId = userId,
            kGram = kGram(),
            statesHolder = StatesHolder(current = currentState, global = globalState)
        )
    }

    /**
     * Sets the state for a user and triggers the state handler.
     *
     * @param userId The ID of the user.
     * @param kGram The [KGram] instance.
     * @param statesHolder The [StatesHolder] containing the new state.
     */
    private suspend fun set(userId: Long, kGram: KGram, statesHolder: StatesHolder<State, GlobalState>) {
        stateStore.set(userId, statesHolder)
        withStateHandler(userId, kGram) { it.handleState() }
    }

    /**
     * Executes a state handler for a user if the state exists.
     *
     * @param userId The ID of the user.
     * @param kGram The [KGram] instance.
     * @param handle A suspend function to handle the state context.
     */
    private suspend fun withStateHandler(
        userId: Long,
        kGram: KGram,
        handle: suspend StateHandler<State, State, GlobalState>.(StateContext<State, State, GlobalState>) -> Unit
    ) {
        val states = stateStore.get(userId) ?: return
        val stateHandler = states.current?.let { it::class }?.let(stateHandlers::get) ?: return

        val stateContext = StateContext(kGram, userId, states)
        stateHandler.handle(stateContext)
        if (stateContext.statesHolder.current != states.current) {
            set(userId, kGram, stateContext.statesHolder)
        } else {
            stateStore.set(userId, stateContext.statesHolder)
        }
    }

    /**
     * Registers a state handler for a specific state class.
     *
     * @param kClass The class of the state to handle.
     * @param handler The [StateHandler] for the state.
     */
    @Suppress("UNCHECKED_CAST")
    fun handleState(kClass: KClass<out State>, handler: StateHandler<State, out State, GlobalState>) {
        stateHandlers[kClass] = handler as StateHandler<State, State, GlobalState>
    }

    /**
     * Registers a state handler for a specific state type.
     *
     * @param ChildState The type of the state to handle.
     * @param handler The [StateHandler] for the state.
     */
    inline fun <reified ChildState : State> handleState(
        handler: StateHandler<State, in ChildState, GlobalState>
    ) {
        handleState(ChildState::class, handler)
    }
}