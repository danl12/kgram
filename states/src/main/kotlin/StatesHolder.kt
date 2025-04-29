package com.github.danl.kgram.states

/**
 * A data class that holds the current and global state for a user.
 *
 * @param State The type of the current state.
 * @param GlobalState The type of the global state.
 * @param current The current state of the user.
 * @param global The global state associated with the user.
 */
data class StatesHolder<out State : Any, out GlobalState : Any>(
    val current: State? = null,
    val global: GlobalState? = null
)