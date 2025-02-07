package ru.danl.kgram.state.store

import ru.danl.kgram.state.States

interface StateStore<State: Any, GlobalState: Any> {

    suspend fun get(userId: Long): States<State, GlobalState>?

    suspend fun set(userId: Long, state: States<State, GlobalState>?)
}