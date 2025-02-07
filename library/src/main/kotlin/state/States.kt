package ru.danl.kgram.state

data class States<out State: Any, out GlobalState: Any>(
    val current: State,
    val global: GlobalState
)