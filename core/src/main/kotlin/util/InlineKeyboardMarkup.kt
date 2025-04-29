package com.github.danl.kgram.util

import org.telegram.telegrambots.meta.api.objects.LoginUrl
import org.telegram.telegrambots.meta.api.objects.games.CallbackGame
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.CopyTextButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.SwitchInlineQueryChosenChat
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo

/**
 * Creates an [InlineKeyboardMarkup] using a builder pattern.
 *
 * @param block A lambda to configure the [InlineKeyboardMarkupBuilder].
 * @return The configured [InlineKeyboardMarkup].
 */
fun inlineKeyboardMarkup(block: InlineKeyboardMarkupBuilder.() -> Unit) =
    InlineKeyboardMarkupBuilder().apply(block).build()

/**
 * A builder class for creating an [InlineKeyboardMarkup].
 */
class InlineKeyboardMarkupBuilder internal constructor() {

    private val rows = mutableListOf<InlineKeyboardRow>()

    /**
     * Adds a row of buttons to the inline keyboard.
     *
     * @param block A lambda to configure the [InlineKeyboardRowBuilder].
     */
    fun row(block: InlineKeyboardRowBuilder.() -> Unit) {
        rows.add(InlineKeyboardRowBuilder().apply(block).build())
    }

    /**
     * Adds a single button as a row to the inline keyboard.
     *
     * @param text The text displayed on the button.
     * @param url The URL to open when the button is clicked.
     * @param callbackData The data sent in a callback query when the button is clicked.
     * @param callbackGame The game to start when the button is clicked.
     * @param switchInlineQuery The inline query to switch to.
     * @param switchInlineQueryCurrentChat The inline query to switch to in the current chat.
     * @param pay Whether the button is a payment button.
     * @param loginUrl The login URL for authentication.
     * @param webApp The web app to open.
     * @param switchInlineQueryChosenChat The inline query for chosen chats.
     * @param copyText The text to copy when the button is clicked.
     */
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

    /**
     * Builds the [InlineKeyboardMarkup] with the configured rows.
     *
     * @return The constructed [InlineKeyboardMarkup].
     */
    fun build() = InlineKeyboardMarkup.builder().keyboard(rows).build()
}

/**
 * A builder class for creating a row of [InlineKeyboardButton]s.
 */
class InlineKeyboardRowBuilder internal constructor() {

    private val buttons = mutableListOf<InlineKeyboardButton>()

    /**
     * Adds a button to the row.
     *
     * @param text The text displayed on the button.
     * @param url The URL to open when the button is clicked.
     * @param callbackData The data sent in a callback query when the button is clicked.
     * @param callbackGame The game to start when the button is clicked.
     * @param switchInlineQuery The inline query to switch to.
     * @param switchInlineQueryCurrentChat The inline query to switch to in the current chat.
     * @param pay Whether the button is a payment button.
     * @param loginUrl The login URL for authentication.
     * @param webApp The web app to open.
     * @param switchInlineQueryChosenChat The inline query for chosen chats.
     * @param copyText The text to copy when the button is clicked.
     */
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

    /**
     * Builds the [InlineKeyboardRow] with the configured buttons.
     *
     * @return The constructed [InlineKeyboardRow].
     */
    fun build() = InlineKeyboardRow(buttons.toList())
}