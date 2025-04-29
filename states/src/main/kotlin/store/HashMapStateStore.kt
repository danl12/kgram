package com.github.danl.kgram.states.store

import com.github.danl.kgram.states.StatesHolder

/**
 * A simple in-memory [StateStore] implementation using a [HashMap].
 *
 * @param State The type of the current state.
 * @param GlobalState The type of the global state.
 */
class HashMapStateStore<State : Any, GlobalState : Any> : StateStore<State, GlobalState> {

    private val hashMap = hashMapOf<Long, StatesHolder<State, GlobalState>>()

    /**
     * Retrieves the state for a user.
     *
     * @param userId The ID of the user.
     * @return The [StatesHolder] containing the user's current and global state, or null if not found.
     */
    override suspend fun get(userId: Long): StatesHolder<State, GlobalState>? = hashMap[userId]

    /**
     * Sets the state for a user.
     *
     * @param userId The ID of the user.
     * @param state The [StatesHolder] containing the user's state, or null to remove the state.
     */
    override suspend fun set(userId: Long, state: StatesHolder<State, GlobalState>?) {
        if (state == null) {
            hashMap.remove(userId)
        } else {
            hashMap[userId] = state
        }
    }
}