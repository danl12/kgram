package ru.danl

import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.danl.kgram.kGram
import ru.danl.kgram.sendPhoto
import ru.danl.kgram.states.StateContext
import ru.danl.kgram.states.StateHandler
import ru.danl.kgram.states.states

sealed class UserState {
    data object WaitingForName : UserState()
    data object WaitingForAge : UserState()
    data object Registered : UserState()
}

// Define global state
data class UserData(val name: String = "", val age: String = "")

// State handler for WaitingForName
object WaitingForNameHandler : StateHandler<UserState, UserState.WaitingForName, UserData> {
    override suspend fun StateContext<UserState, UserState.WaitingForName, UserData>.handleMessage(message: Message) {
        val name = message.text ?: return
        updateGlobalState { it.copy(name = name) }
        setCurrentState(UserState.WaitingForAge)
        kGram.sendPhoto {
            chatId(message.chatId.toString())
            photo(InputFile("https://example.com/name_received.jpg"))
            caption("Got your name! Now, please send your age.")
        }
    }

    override suspend fun StateContext<UserState, UserState.WaitingForName, UserData>.handleState() {
        kGram.sendPhoto {
            chatId(userId.toString())
            photo(InputFile("https://example.com/welcome.jpg"))
            caption("Please send your name.")
        }
    }
}

// State handler for WaitingForAge
object WaitingForAgeHandler : StateHandler<UserState, UserState.WaitingForAge, UserData> {
    override suspend fun StateContext<UserState, UserState.WaitingForAge, UserData>.handleMessage(message: Message) {
        val age = message.text ?: return
        updateGlobalState { it.copy(age = age) }
        setCurrentState(UserState.Registered)
        kGram.sendPhoto {
            chatId(message.chatId.toString())
            photo(InputFile("https://example.com/registered.jpg"))
            caption("Registration complete! Name: ${globalState.name}, Age: $age")
        }
    }

    override suspend fun StateContext<UserState, UserState.WaitingForAge, UserData>.handleState() {
        kGram.sendPhoto {
            chatId(userId.toString())
            photo(InputFile("https://example.com/age_prompt.jpg"))
            caption("Please send your age.")
        }
    }
}

// State handler for Registered
object RegisteredHandler : StateHandler<UserState, UserState.Registered, UserData> {
    override suspend fun StateContext<UserState, UserState.Registered, UserData>.handleState() {
        kGram.sendPhoto {
            chatId(userId.toString())
            photo(InputFile("https://example.com/thank_you.jpg"))
            caption("You're registered! Name: ${globalState.name}, Age: ${globalState.age}")
        }
    }
}

fun main(): Unit = runBlocking {
    kGram(token = "YOUR_BOT_TOKEN") {
        // Configure state management using the states module
        states {
            handleState<UserState.WaitingForName>(WaitingForNameHandler)
            handleState<UserState.WaitingForAge>(WaitingForAgeHandler)
            handleState<UserState.Registered>(RegisteredHandler)

            // Handle initial message to start registration
            handleMessage { message ->
                set(message.from.id, UserState.WaitingForName, UserData())
            }
        }
    }.start() // Start the bot
}