package ru.danl.kgram

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal class KGramUpdateContext(
    val kGram: KGram
) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = KGramUpdateContext

    companion object : CoroutineContext.Key<KGramUpdateContext>
}

suspend fun kGram() = requireNotNull(coroutineContext[KGramUpdateContext]).kGram