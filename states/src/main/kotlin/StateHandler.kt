package com.danl.kgram.states

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.Message

/**
 * Interface for handling state-specific logic in a Telegram bot.
 *
 * @param ParentState The parent type of the state.
 * @param State The specific state type, which is a subtype of [ParentState].
 * @param GlobalState The type of the global state.
 */
interface StateHandler<ParentState : Any, State : ParentState, GlobalState : Any> {

    /**
     * Handles the current state.
     *
     * @param receiver The [StateContext] containing the state and bot information.
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleState()

    /**
     * Handles an incoming message for the current state.
     *
     * @param receiver The [StateContext] containing the state and bot information.
     * @param message The incoming message.
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleMessage(message: Message) = Unit

    /**
     * Handles an edited message for the current state.
     *
     * @param receiver The [StateContext] containing the state and bot information.
     * @param message The edited message.
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleEditedMessage(message: Message) = Unit

    /**
     * Handles a callback query for the current state.
     *
     * @param receiver The [StateContext] containing the state and bot information.
     * @param callbackQuery The callback query.
     */
    suspend fun StateContext<ParentState, State, GlobalState>.handleCallbackQuery(callbackQuery: CallbackQuery) = Unit
}