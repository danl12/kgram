package ru.danl

import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.message.Message
import ru.danl.kgram.send
import ru.danl.kgram.kGram
import ru.danl.kgram.sendPhoto
import ru.danl.kgram.util.inlineKeyboardMarkup

fun main(): Unit = runBlocking {
    kGram(token = "7437021878:AAHKZARtZpZGp767u5GJ7rqG9o9T6zFyC9A") {
        // Handle incoming messages
        handleMessage { message: Message ->
            sendPhoto {
                chatId(message.chatId.toString())
                photo(InputFile("https://example.com/image.jpg"))
                caption("Hello, ${message.from.firstName}!")
                replyMarkup(inlineKeyboardMarkup {
                    row {
                        button("Visit Website", url = "https://example.com")
                        button("Say Hi", callbackData = "hi")
                    }
                })
            }
        }

        // Handle callback queries from inline buttons
        handleCallbackQuery { callbackQuery ->
            if (callbackQuery.data == "hi") {
                sendPhoto {
                    chatId(callbackQuery.message.chatId.toString())
                    photo(InputFile("https://example.com/hi.jpg"))
                    caption("You said hi!")
                }
            }
        }
    }.start() // Start the bot
}