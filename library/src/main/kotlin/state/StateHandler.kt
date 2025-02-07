package ru.danl.kgram.state

import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.message.Message

interface StateHandler<ParentState: Any, State: ParentState, GlobalState: Any> {

    suspend fun StateContext<ParentState, State, GlobalState>.handleState()

    suspend fun StateContext<ParentState, State, GlobalState>.handleMessage(message: Message) = Unit

    suspend fun StateContext<ParentState, State, GlobalState>.handleCallbackQuery(callbackQuery: CallbackQuery) = Unit

}