package ru.danl.kgram.handler

import org.telegram.telegrambots.meta.api.objects.Update
import ru.danl.kgram.KGram

internal fun interface UpdateHandler {

    suspend fun handleUpdate(kgram: KGram, update: Update)

}