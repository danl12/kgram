package ru.danl.kgram.state.store

import ru.danl.kgram.state.States

class HashMapStateStore<State : Any, GlobalState : Any> : StateStore<State, GlobalState> {

    private val hashMap = hashMapOf<Long, States<State, GlobalState>>()

    override suspend fun get(userId: Long): States<State, GlobalState>? = hashMap[userId]

    override suspend fun set(userId: Long, state: States<State, GlobalState>?) {
        if (state == null) {
            hashMap.remove(userId)
        } else {
            hashMap[userId] = state
        }
    }
}