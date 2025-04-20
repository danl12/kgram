package ru.danl.kgram.state.store

import ru.danl.kgram.state.StatesHolder

/**
 * An in-memory implementation of [StateStore] using a [HashMap] to store state per user.
 *
 * This implementation is suitable for development or simple use cases where persistent
 * storage is not required. The state will be lost when the application is stopped or restarted.
 *
 * @param State The type representing the current user state.
 * @param GlobalState The type representing the global user state.
 */
class HashMapStateStore<State : Any, GlobalState : Any> : StateStore<State, GlobalState> {

    private val hashMap = hashMapOf<Long, StatesHolder<State, GlobalState>>()

    /**
     * Retrieves the current state for the specified user from memory.
     *
     * @param userId The unique identifier of the user.
     * @return A [StatesHolder] containing the user's state and the global user's state, or `null` if no state is stored.
     */
    override suspend fun get(userId: Long): StatesHolder<State, GlobalState>? = hashMap[userId]

    /**
     * Stores or removes the state for the specified user in memory.
     *
     * @param userId The unique identifier of the user.
     * @param state A [StatesHolder] containing the user's new state and global user's state, or `null` to remove the stored state.
     */
    override suspend fun set(userId: Long, state: StatesHolder<State, GlobalState>?) {
        if (state == null) {
            hashMap.remove(userId)
        } else {
            hashMap[userId] = state
        }
    }
}