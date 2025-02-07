package ru.danl.kgram.state

import ru.danl.kgram.KGram
import ru.danl.kgram.kGram
import ru.danl.kgram.state.store.HashMapStateStore
import ru.danl.kgram.state.store.StateStore
import kotlin.reflect.KClass

fun <State : Any, GlobalState : Any> KGram.Config.handleStates(
    stateStore: StateStore<State, GlobalState> = HashMapStateStore(),
    configure: HandleStates<State, GlobalState>.() -> Unit
) = HandleStates(stateStore, this).apply(configure)

class HandleStates<State : Any, GlobalState : Any> internal constructor(
    private val stateStore: StateStore<State, GlobalState>,
    kGramConfig: KGram.Config
) {
    private val stateHandlers = mutableMapOf<KClass<out State>, StateHandler<State, State, GlobalState>>()

    init {
        kGramConfig.handleMessage { message ->
            val userId = message.from.id

            withStateHandler(userId, this) { it.handleMessage(message) }
        }
        kGramConfig.handleCallbackQuery { callbackQuery ->
            val userId = callbackQuery.from.id

            withStateHandler(userId, this) { it.handleCallbackQuery(callbackQuery) }
        }
    }

    suspend fun getStates(userId: Long) = stateStore.get(userId)

    suspend fun setCurrentState(
        userId: Long,
        currentState: State
    ) {
        setStates(
            userId = userId,
            kGram = kGram(),
            states = stateStore.get(userId)?.copy(current = currentState) ?: return
        )
    }

    suspend fun setGlobalState(
        userId: Long,
        globalState: GlobalState
    ) {
        setStates(
            userId = userId,
            kGram = kGram(),
            states = stateStore.get(userId)?.copy(global = globalState) ?: return
        )
    }

    suspend fun setStates(
        userId: Long,
        currentState: State,
        globalState: GlobalState
    ) {
        setStates(
            userId = userId,
            kGram = kGram(),
            states = States(
                currentState,
                globalState
            )
        )
    }

    private suspend fun setStates(
        userId: Long,
        kGram: KGram,
        states: States<State, GlobalState>
    ) {
        stateStore.set(userId, states)
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
        if (stateContext.states.current != states.current) setStates(userId, kGram, stateContext.states)
        else stateStore.set(userId, stateContext.states)
    }

    fun handleState(kClass: KClass<out State>, handler: StateHandler<State, out State, GlobalState>) {
        stateHandlers[kClass] = handler as StateHandler<State, State, GlobalState>
    }

    inline fun <reified ChildState : State> handleState(
        handler: StateHandler<State, in ChildState, GlobalState>
    ) {
        handleState(ChildState::class, handler)
    }
}