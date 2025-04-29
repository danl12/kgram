package ru.danl.kgram.states

import ru.danl.kgram.KGram

/**
 * A context class for managing state-related operations in a Telegram bot.
 *
 * @param ParentState The parent type of the state.
 * @param State The specific state type, which is a subtype of [ParentState].
 * @param GlobalState The type of the global state.
 * @param kGram The [KGram] instance for interacting with the Telegram Bot API.
 * @param userId The ID of the user associated with this context.
 * @param statesHolder The [StatesHolder] containing the current and global state.
 */
class StateContext<ParentState : Any, State : ParentState, GlobalState : Any> internal constructor(
    val kGram: KGram,
    val userId: Long,
    internal var statesHolder: StatesHolder<ParentState, GlobalState>,
) {

    /**
     * The current state of the user.
     */
    @Suppress("UNCHECKED_CAST")
    val currentState: State
        get() = statesHolder.current as State

    /**
     * The global state associated with the user.
     */
    val globalState: GlobalState
        get() = checkNotNull(statesHolder.global)

    /**
     * Sets the current state for the user.
     *
     * @param state The new current state.
     */
    fun setCurrentState(state: ParentState) {
        statesHolder = statesHolder.copy(current = state)
    }

    /**
     * Sets the global state for the user.
     *
     * @param globalState The new global state.
     */
    fun setGlobalState(globalState: GlobalState) {
        statesHolder = statesHolder.copy(global = globalState)
    }

    /**
     * Updates the current state using a transformation function.
     *
     * @param update A function that transforms the current state.
     */
    fun updateCurrentState(update: (State) -> ParentState) {
        statesHolder = statesHolder.copy(current = currentState.let(update))
    }

    /**
     * Updates the global state using a transformation function.
     *
     * @param update A function that transforms the global state.
     */
    fun updateGlobalState(update: (GlobalState) -> GlobalState) {
        statesHolder = statesHolder.copy(global = globalState.let(update))
    }
}