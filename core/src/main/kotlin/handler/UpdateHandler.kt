package com.danl.kgram.handler

import org.telegram.telegrambots.meta.api.objects.Update

internal fun interface UpdateHandler {

    suspend fun handleUpdate(update: Update)
}