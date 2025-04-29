package com.github.danl.kgram.states.store

import com.github.danl.kgram.states.StatesHolder

/**
 * Interface for storing and retrieving user states in a Telegram bot.
 *
 * @param State The type of the current state.
 * @param GlobalState The type of the global state.
 */
interface StateStore<State: Any, GlobalState: Any> {

    /**
     * Retrieves the state for a user.
     *
     * @param userId The ID of the user.
     * @return The [StatesHolder] containing the user's current and global state, or null if not found.
     */
    suspend fun get(userId: Long): StatesHolder<State, GlobalState>?

    /**
     * Sets the state for a user.
     *
     * @param userId The ID of the user.
     * @param state The [StatesHolder] containing the user's state, or null to remove the state.
     */
    suspend fun set(userId: Long, state: StatesHolder<State, GlobalState>?)
}