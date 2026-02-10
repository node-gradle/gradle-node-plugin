package com.github.gradle.node.util

import org.gradle.api.provider.Provider

internal fun <A, B> zip(aProvider: Provider<A>, bProvider: Provider<B>): Provider<Pair<A, B>> {
    return aProvider.flatMap { a -> bProvider.map { b -> Pair(a, b) } }
}

internal fun <A, B, C> zip(aProvider: Provider<A>, bProvider: Provider<B>, cProvider: Provider<C>):
        Provider<Triple<A, B, C>> {
    return zip(aProvider, bProvider).flatMap { pair -> cProvider.map { c -> Triple(pair.first, pair.second, c) } }
}

internal fun <A, B, C, D> zip(
    aProvider: Provider<A>, bProvider: Provider<B>, cProvider: Provider<C>,
    dProvider: Provider<D>
): Provider<Tuple4<A, B, C, D>> {
    return zip(zip(aProvider, bProvider), zip(cProvider, dProvider))
        .map { pairs -> Tuple4(pairs.first.first, pairs.first.second, pairs.second.first, pairs.second.second) }
}

internal data class Tuple4<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

internal fun <A, B, C, D, E> zip(
    aProvider: Provider<A>, bProvider: Provider<B>, cProvider: Provider<C>,
    dProvider: Provider<D>, eProvider: Provider<E>
): Provider<Tuple5<A, B, C, D, E>> {
    return zip(zip(aProvider, bProvider), zip(cProvider, dProvider, eProvider))
        .map { pairs ->
            Tuple5(
                pairs.first.first, pairs.first.second, pairs.second.first, pairs.second.second,
                pairs.second.third
            )
        }
}

internal data class Tuple5<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
