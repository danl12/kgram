package ru.danl

import kotlinx.coroutines.runBlocking
import ru.danl.kgram.startKGram
import ru.danl.kgram.state.StateContext
import ru.danl.kgram.state.StateHandler
import ru.danl.kgram.state.handleStates

data class ExampleGlobalState(
    val userId: Long
)

sealed interface ExampleState {

    data class Init(val messageId: Long? = null): ExampleState

}

fun main(): Unit = runBlocking {
    startKGram("TOKEN") {
        handleStates<ExampleState, ExampleGlobalState> {
            handleState(InitStateHandler())

            handleMessage {
                val userId = it.from.id
                setStates(userId, ExampleState.Init(), ExampleGlobalState(userId))
            }
        }
    }
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