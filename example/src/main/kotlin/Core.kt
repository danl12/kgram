package com.github.danl

import kotlinx.coroutines.runBlocking
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.message.Message
import com.danl.kgram.send
import com.danl.kgram.kGram
import com.danl.kgram.sendPhoto
import com.danl.kgram.util.inlineKeyboardMarkup

fun main(): Unit = runBlocking {
    kGram(token = "YOUR_BOT_TOKEN") {
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