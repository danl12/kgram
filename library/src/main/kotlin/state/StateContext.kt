package ru.danl.kgram.state

import ru.danl.kgram.KGram

class StateContext<ParentState: Any, State: ParentState, GlobalState: Any>(
    val kGram: KGram,
    val userId: Long,
    internal var states: States<ParentState, GlobalState>,
) {

    @Suppress("UNCHECKED_CAST")
    val currentState: State
        get() = states.current as State

    val globalState: GlobalState
        get() = states.global

    fun setCurrentState(state: ParentState) {
        states = states.copy(current = state)
    }

    fun setGlobalState(globalState: GlobalState) {
        states = states.copy(global = globalState)
    }

    fun updateCurrentState(update: (State) -> ParentState) {
        states = states.copy(current = currentState.let(update))
    }

    fun updateGlobalState(update: (GlobalState) -> GlobalState) {
        states = states.copy(global = globalState.let(update))
    }
}