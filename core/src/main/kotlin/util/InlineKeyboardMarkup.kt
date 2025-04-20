package ru.danl.kgram.util

import org.telegram.telegrambots.meta.api.objects.LoginUrl
import org.telegram.telegrambots.meta.api.objects.games.CallbackGame
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.CopyTextButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.SwitchInlineQueryChosenChat
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo

/**
 * DSL entry point for building an [InlineKeyboardMarkup].
 *
 * Example usage:
 * ```
 * val markup = inlineKeyboardMarkup {
 *     row {
 *         button(text = "Click me", callbackData = "action_click")
 *     }
 *     button(text = "Single row button", url = "https://example.com")
 * }
 * ```
 *
 * @param block A configuration block used to define the inline keyboard layout.
 * @return A fully constructed [InlineKeyboardMarkup] object.
 */
fun inlineKeyboardMarkup(block: InlineKeyboardMarkupBuilder.() -> Unit) =
    InlineKeyboardMarkupBuilder().apply(block).build()

/**
 * Builder class for constructing [InlineKeyboardMarkup] layouts.
 *
 * This class allows creating a keyboard layout composed of multiple rows.
 * Each row can contain one or more inline buttons.
 */
class InlineKeyboardMarkupBuilder internal constructor() {

    private val rows = mutableListOf<InlineKeyboardRow>()

    /**
     * Adds a new row to the inline keyboard.
     *
     * @param block A block to configure buttons within the row.
     */
    fun row(block: InlineKeyboardRowBuilder.() -> Unit) {
        rows.add(InlineKeyboardRowBuilder().apply(block).build())
    }

    /**
     * Adds a new row containing a single button.
     *
     * This is a shorthand for adding a row with one button.
     *
     * @param text The button text.
     * @param url HTTP or tg:// URL to be opened when the button is pressed.
     * @param callbackData Data to be sent in a callback query to the bot when button is pressed.
     * @param callbackGame Description of the game that will be launched when the user presses the button.
     * @param switchInlineQuery If set, pressing the button will prompt the user to select one of their chats, open that chat and insert the bot‘s username and the specified inline query in the input field.
     * @param switchInlineQueryCurrentChat Similar to switchInlineQuery, but inserts the bot's username and query in the current chat’s input field.
     * @param pay If true, the button will be a Pay button.
     * @param loginUrl An HTTPS URL used to automatically authorize the user.
     * @param webApp Information about the Web App to launch when the user presses the button.
     * @param switchInlineQueryChosenChat If set, allows the user to switch to a different chat for inline queries.
     * @param copyText If set, allows copying text to clipboard.
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
     * Builds and returns the final [InlineKeyboardMarkup] instance.
     *
     * @return A fully constructed inline keyboard markup object.
     */
    fun build() = InlineKeyboardMarkup.builder().keyboard(rows).build()
}

/**
 * Builder class for creating a single row of inline keyboard buttons.
 *
 * Each row consists of one or more [InlineKeyboardButton]s.
 */
class InlineKeyboardRowBuilder internal constructor() {

    private val buttons = mutableListOf<InlineKeyboardButton>()

    /**
     * Adds a button to the current row.
     *
     * @param text The button text.
     * @param url HTTP or tg:// URL to be opened when the button is pressed.
     * @param callbackData Data to be sent in a callback query to the bot when button is pressed.
     * @param callbackGame Description of the game that will be launched when the user presses the button.
     * @param switchInlineQuery If set, pressing the button will prompt the user to select one of their chats, open that chat and insert the bot‘s username and the specified inline query in the input field.
     * @param switchInlineQueryCurrentChat Similar to switchInlineQuery, but inserts the bot's username and query in the current chat’s input field.
     * @param pay If true, the button will be a Pay button.
     * @param loginUrl An HTTPS URL used to automatically authorize the user.
     * @param webApp Information about the Web App to launch when the user presses the button.
     * @param switchInlineQueryChosenChat If set, allows the user to switch to a different chat for inline queries.
     * @param copyText If set, allows copying text to clipboard.
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
     * Builds and returns a row containing the added buttons.
     *
     * @return An [InlineKeyboardRow] containing the buttons defined in this builder.
     */
    fun build() = InlineKeyboardRow(buttons.toList())
}