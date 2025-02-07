package ru.danl.kgram.util

import org.telegram.telegrambots.meta.api.objects.LoginUrl
import org.telegram.telegrambots.meta.api.objects.games.CallbackGame
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.CopyTextButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.SwitchInlineQueryChosenChat
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo

fun inlineKeyboardMarkup(block: InlineKeyboardMarkupBuilder.() -> Unit) =
    InlineKeyboardMarkupBuilder().apply(block).build()

class InlineKeyboardMarkupBuilder internal constructor() {

    private val rows = mutableListOf<InlineKeyboardRow>()

    fun row(block: InlineKeyboardRowBuilder.() -> Unit) {
        rows.add(InlineKeyboardRowBuilder().apply(block).build())
    }

    fun button(
        text: String,
        url: String? = null,
        callbackData: String? = null,
        callbackGame: CallbackGame? = null,
        switchInlineQuery: String? = null,
        switchInlineQueryCurrentChat: String? = null,
        pay: Boolean? = null,
        loginUrl: LoginUrl? = null,
        webApp: WebAppInfo? = null,
        switchInlineQueryChosenChat: SwitchInlineQueryChosenChat? = null,
        copyText: CopyTextButton? = null
    ) {
        val inlineKeyboardRowBuilder = InlineKeyboardRowBuilder()
        inlineKeyboardRowBuilder.button(
            text,
            url,
            callbackData,
            callbackGame,
            switchInlineQuery,
            switchInlineQueryCurrentChat,
            pay,
            loginUrl,
            webApp,
            switchInlineQueryChosenChat,
            copyText
        )
        rows.add(inlineKeyboardRowBuilder.build())
    }

    fun build() = InlineKeyboardMarkup.builder().keyboard(rows).build()
}

class InlineKeyboardRowBuilder internal constructor()  {

    private val buttons = mutableListOf<InlineKeyboardButton>()

    fun button(
        text: String,
        url: String? = null,
        callbackData: String? = null,
        callbackGame: CallbackGame? = null,
        switchInlineQuery: String? = null,
        switchInlineQueryCurrentChat: String? = null,
        pay: Boolean? = null,
        loginUrl: LoginUrl? = null,
        webApp: WebAppInfo? = null,
        switchInlineQueryChosenChat: SwitchInlineQueryChosenChat? = null,
        copyText: CopyTextButton? = null
    ) {
        buttons.add(
            InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .callbackData(callbackData)
                .callbackGame(callbackGame)
                .switchInlineQuery(switchInlineQuery)
                .switchInlineQueryCurrentChat(switchInlineQueryCurrentChat)
                .pay(pay)
                .loginUrl(loginUrl)
                .webApp(webApp)
                .switchInlineQueryChosenChat(switchInlineQueryChosenChat)
                .copyText(copyText)
                .build()
        )
    }

    fun build() = InlineKeyboardRow(buttons.toList())
}