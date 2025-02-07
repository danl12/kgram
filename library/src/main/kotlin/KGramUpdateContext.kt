package ru.danl.kgram

import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class KGramUpdateContext(
    val kGram: KGram
): CoroutineContext.Element {

    private val _isHandled: AtomicBoolean = AtomicBoolean(false)
    var isHandled: Boolean
        set(value) = _isHandled.set(value)
        get() = _isHandled.get()

    override val key: CoroutineContext.Key<*> = KGramUpdateContext

    companion object : CoroutineContext.Key<KGramUpdateContext>
}

suspend fun isUpdateHandled() = requireNotNull(coroutineContext[KGramUpdateContext]).isHandled

suspend fun updateHandled() {
    requireNotNull(coroutineContext[KGramUpdateContext]).isHandled = true
}

suspend fun kGram() = requireNotNull(coroutineContext[KGramUpdateContext]).kGram