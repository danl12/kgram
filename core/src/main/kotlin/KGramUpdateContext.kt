package ru.danl.kgram

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal class KGramUpdateContext(
    val kGram: KGram
) : CoroutineContext.Element {

    override val key: CoroutineContext.Key<*> = KGramUpdateContext

    companion object : CoroutineContext.Key<KGramUpdateContext>
}

/**
 * Retrieves the [KGram] instance from the current coroutine context.
 *
 * @return The [KGram] instance associated with the current [KGramUpdateContext].
 * @throws IllegalStateException If the [KGramUpdateContext] is not present in the coroutine context.
 */
suspend fun kGram() = requireNotNull(coroutineContext[KGramUpdateContext]).kGram