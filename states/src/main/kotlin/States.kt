package ru.danl.kgram.states

import ru.danl.kgram.KGram
import ru.danl.kgram.kGram
import ru.danl.kgram.states.store.HashMapStateStore
import ru.danl.kgram.states.store.StateStore
import kotlin.reflect.KClass

/**
 * Configures and initializes state management for the [KGram] Telegram bot instance.
 *
 * This extension function provides a DSL for defining state-based handlers using a customizable
 * [StateStore]. By default, it uses an in-memory [HashMapStateStore].
 *
 * @param stateStore The storage implementation used for persisting state across user sessions.
 * @param configure A configuration block where you can register state handlers.
 * @return An instance of [States] used for managing current and global states.
 */
fun <State : Any, GlobalState : Any> KGram.states(
    stateStore: StateStore<State, GlobalState> = HashMapStateStore(),
    configure: States<State, GlobalState>.() -> Unit
) = States(stateStore, this).apply(configure)

/**
 * Manages finite state machine (FSM) logic for a Telegram bot.
 *
 * This class enables tracking user states and a global user state, handling
 * updates through associated [StateHandler]s based on the current state.
 *
 * @param State The type representing the user-specific state.
 * @param GlobalState The type representing the global user state.
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
     * Retrieves the current [StatesHolder] for the given user.
     *
     * @param userId The unique identifier of the user.
     * @return The current state holder or `null` if no state is stored.
     */
    suspend fun get(userId: Long) = stateStore.get(userId)

    /**
     * Updates the current user state while keeping the global user state unchanged.
     *
     * @param userId The unique identifier of the user.
     * @param currentState The new current state.
     */
    suspend fun setCurrentState(userId: Long, currentState: State) {
        set(
            userId = userId,
            kGram = kGram(),
            statesHolder = stateStore.get(userId)?.copy(current = currentState) ?: return
        )
    }

    /**
     * Updates the global user state while keeping the current user state unchanged.
     *
     * @param userId The unique identifier of the user.
     * @param globalState The new global user state.
     */
    suspend fun setGlobalState(userId: Long, globalState: GlobalState) {
        set(
            userId = userId,
            kGram = kGram(),
            statesHolder = stateStore.get(userId)?.copy(global = globalState) ?: return
        )
    }

    /**
     * Sets both the user state and the global user state.
     *
     * @param userId The unique identifier of the user.
     * @param currentState The new current state.
     * @param globalState The new global state.
     */
    suspend fun set(userId: Long, currentState: State, globalState: GlobalState) {
        set(
            userId = userId,
            kGram = kGram(),
            statesHolder = StatesHolder(currentState, globalState)
        )
    }

    private suspend fun set(userId: Long, kGram: KGram, statesHolder: StatesHolder<State, GlobalState>) {
        stateStore.set(userId, statesHolder)
        withStateHandler(userId, kGram) { it.handleState() }
    }

    private suspend fun withStateHandler(
        userId: Long,
        kGram: KGram,
        handle: suspend StateHandler<State, State, GlobalState>.(StateContext<State, State, GlobalState>) -> Unit
    ) {
        val states = stateStore.get(userId) ?: return
        val stateHandler = stateHandlers[states.current::class] ?: return

        val stateContext = StateContext(kGram, userId, states)
        stateHandler.handle(stateContext)
        if (stateContext.statesHolder.current != states.current) {
            set(userId, kGram, stateContext.statesHolder)
        } else {
            stateStore.set(userId, stateContext.statesHolder)
        }
    }

    /**
     * Registers a [StateHandler] for a specific current state class.
     *
     * @param kClass The class of the state to handle.
     * @param handler The handler instance responsible for processing that state.
     */
    @Suppress("UNCHECKED_CAST")
    fun handleState(kClass: KClass<out State>, handler: StateHandler<State, out State, GlobalState>) {
        stateHandlers[kClass] = handler as StateHandler<State, State, GlobalState>
    }

    /**
     * Registers a [StateHandler] for a specific state using a reified generic.
     *
     * @param handler The handler instance responsible for the specified child state.
     */
    inline fun <reified ChildState : State> handleState(
        handler: StateHandler<State, in ChildState, GlobalState>
    ) {
        handleState(ChildState::class, handler)
    }
}