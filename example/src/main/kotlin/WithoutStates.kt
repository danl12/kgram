package ru.danl

import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.danl.kgram.send
import ru.danl.kgram.startKGram

fun main(): Unit = runBlocking {
    startKGram("TOKEN") {
        handleMessage { message ->
            message.text?.takeIf { it.isNotBlank() }?.let { text ->
                send(SendMessage::builder) {
                    text("Hello $text!")
                }
            }
        }
        handleMessage(filter = Message::isCommand) {
            // handle command
        }
        handleMessage(filter = { it.text?.startsWith("/start") == true }) {
            // handle start command
        }
    }
}