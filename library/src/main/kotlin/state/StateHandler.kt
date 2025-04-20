package ru.danl.kgram.state

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.Message

/**
 * Defines how to handle interactions and transitions for a specific state within a Telegram bot.
 *
 * This interface provides lifecycle hooks that are invoked when the bot is in a specific state
 * and receives different types of updates (messages, edited messages, or callback queries).
 *
 * @param ParentState The base type for all possible states.
 * @param State The specific state this handler is responsible for. Must be a subtype of [ParentState].
 * @param GlobalState The type representing the global user state.
 */
interface StateHandler<ParentState : Any, State : ParentState, GlobalState : Any> {

    /**
     * Called when entering or processing the given [State].
     *
     * Override this method to define the default behavior of the bot while in this state.
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleState()

    /**
     * Called when a regular message is received while in the given [State].
     *
     * Override to define message-specific logic. Default implementation does nothing.
     *
     * @param message The received [Message].
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleMessage(message: Message) = Unit

    /**
     * Called when an edited message is received while in the given [State].
     *
     * Override to define logic for handling edited messages. Default implementation does nothing.
     *
     * @param message The edited [Message].
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleEditedMessage(message: Message) = Unit

    /**
     * Called when a callback query is received while in the given [State].
     *
     * Override to define logic for handling callback queries (e.g., inline keyboard interactions).
     * Default implementation does nothing.
     *
     * @param callbackQuery The received [CallbackQuery].
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleCallbackQuery(callbackQuery: CallbackQuery) = Unit
}