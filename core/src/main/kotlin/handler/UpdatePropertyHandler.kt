package com.danl.kgram.handler

import org.telegram.telegrambots.meta.api.objects.Update
import com.danl.kgram.KGram
import com.danl.kgram.kGram

internal class UpdatePropertyHandler<T : Any>(
    private val getProperty: (Update) -> T?,
    private val filter: suspend (T) -> Boolean = { true },
    private val handleProperty: suspend (KGram, T) -> Unit
) : UpdateHandler {

    override suspend fun handleUpdate(update: Update) {
        update.let(getProperty)?.takeIf { filter(it) }?.let { handleProperty(kGram(), it) }
    }
}