package ru.danl

import kotlinx.coroutines.runBlocking
import ru.danl.kgram.kGram
import ru.danl.kgram.state.StateContext
import ru.danl.kgram.state.StateHandler
import ru.danl.kgram.state.states
import ru.danl.state.states

data class ExampleGlobalState(
    val userId: Long
)

sealed interface ExampleState {

    data class Init(val messageId: Long? = null): ExampleState

}

fun main(): Unit = runBlocking {
    kGram("TOKEN") {
        states {
            handleState(InitStateHandler())

            handleMessage(filter = { it.text?.startsWith("/start") == true }) {
                val userId = it.from.id
                this@states.set(userId, ExampleState.Init(), ExampleGlobalState(userId))
            }
        }
    }.start()
}

class InitStateHandler: StateHandler<ExampleState, ExampleState.Init, ExampleGlobalState> {

    override suspend fun StateContext<ExampleState, ExampleState.Init, ExampleGlobalState>.handleState() {
        if (currentState.messageId == null) {
            // do something
        } else {
            // do something
        }
    }
}