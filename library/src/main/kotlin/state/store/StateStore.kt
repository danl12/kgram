package ru.danl.kgram.state.store

import ru.danl.kgram.state.StatesHolder

/**
 * A generic interface for storing and retrieving user-specific state in a Telegram bot.
 *
 * @param State The type representing the current user state.
 * @param GlobalState The type representing the global user state.
 */
interface StateStore<State: Any, GlobalState: Any> {

    /**
     * Retrieves the [StatesHolder] for the specified user.
     *
     * @param userId The unique identifier of the user.
     * @return A [StatesHolder] containing the user's state and the global user's state, or `null` if no state is stored.
     */
    suspend fun get(userId: Long): StatesHolder<State, GlobalState>?

    /**
     * Sets or updates the state for the specified user.
     *
     * @param userId The unique identifier of the user.
     * @param state A [StatesHolder] containing the user's new state and the global user's state, or `null` to clear the stored state.
     */
    suspend fun set(userId: Long, state: StatesHolder<State, GlobalState>?)
}