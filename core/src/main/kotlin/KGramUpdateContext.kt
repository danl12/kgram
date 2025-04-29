package com.github.danl.kgram

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * A coroutine context element that holds a reference to a [KGram] instance.
 * Used to provide access to the [KGram] instance within coroutine scopes.
 *
 * @param kGram The [KGram] instance associated with this context.
 */
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