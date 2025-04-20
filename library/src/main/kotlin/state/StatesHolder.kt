package ru.danl.kgram.state

/**
 * A container that holds both the user-specific state and a user-specific global state.
 *
 * @param State The type representing the current user state.
 * @param GlobalState The type representing the global user state.
 * @property current The current state associated with the user.
 * @property global The global state associated with the user.
 */
data class StatesHolder<out State : Any, out GlobalState : Any>(
    val current: State,
    val global: GlobalState
)