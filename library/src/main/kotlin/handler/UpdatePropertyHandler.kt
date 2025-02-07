package ru.danl.kgram.handler

import org.telegram.telegrambots.meta.api.objects.Update
import ru.danl.kgram.KGram

internal class UpdatePropertyHandler<T: Any>(
    private val getProperty: (Update) -> T?,
    private val filter: suspend (T) -> Boolean = { true },
    private val handleProperty: suspend (KGram, T) -> Unit
): UpdateHandler {

    override suspend fun handleUpdate(kgram: KGram, update: Update) {
        update.let(getProperty)?.takeIf { filter(it) } ?.let { handleProperty(kgram, it) }
    }
}