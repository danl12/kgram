package ru.danl.kgram.states

import ru.danl.kgram.KGram

/**
 * Represents the context in which a specific bot state is handled.
 *
 * Provides access to the current user ID, the current and global state,
 * and utility methods for modifying state during execution of a [StateHandler].
 *
 * @param ParentState The base type for all possible states.
 * @param State The specific type of the current state (a subtype of [ParentState]).
 * @param GlobalState The type representing the global user state.
 * @property kGram The [KGram] instance used to interact with the Telegram API.
 * @property userId The unique identifier of the user this context is associated with.
 * @property statesHolder A holder object containing both current and global state.
 */
class StateContext<ParentState : Any, State : ParentState, GlobalState : Any> internal constructor(
    val kGram: KGram,
    val userId: Long,
    internal var statesHolder: StatesHolder<ParentState, GlobalState>,
) {

    /**
     * The current state, cast to the specific [State] subtype.
     */
    @Suppress("UNCHECKED_CAST")
    val currentState: State
        get() = statesHolder.current as State

    /**
     * The current global user state.
     */
    val globalState: GlobalState
        get() = statesHolder.global

    /**
     * Replaces the current user-specific state with a new one.
     *
     * @param state The new state to set.
     */
    fun setCurrentState(state: ParentState) {
        statesHolder = statesHolder.copy(current = state)
    }

    /**
     * Replaces the global user state with a new one.
     *
     * @param globalState The new global state to set.
     */
    fun setGlobalState(globalState: GlobalState) {
        statesHolder = statesHolder.copy(global = globalState)
    }

    /**
     * Updates the current user-specific state using the provided transformation.
     *
     * @param update A function that takes the current state and returns an updated one.
     */
    fun updateCurrentState(update: (State) -> ParentState) {
        statesHolder = statesHolder.copy(current = currentState.let(update))
    }

    /**
     * Updates the global user state using the provided transformation.
     *
     * @param update A function that takes the current global state and returns an updated one.
     */
    fun updateGlobalState(update: (GlobalState) -> GlobalState) {
        statesHolder = statesHolder.copy(global = globalState.let(update))
    }
}