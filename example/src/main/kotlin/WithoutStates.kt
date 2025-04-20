package ru.danl

import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.danl.kgram.send
import ru.danl.kgram.kGram

fun main(): Unit = runBlocking {
    kGram("TOKEN") {
        handleMessage { message ->
            message.text?.takeIf { it.isNotBlank() }?.let { text ->
                send(SendMessage::builder) {
                    chatId(message.chatId)
                    text(text)
                }
            }
        }
        handleMessage(filter = Message::isCommand) {
            // handle command
        }
        handleMessage(filter = { it.text?.startsWith("/start") == true }) {
            // handle start command
        }
    }.start()
}