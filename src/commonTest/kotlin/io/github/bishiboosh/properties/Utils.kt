package io.github.bishiboosh.properties

import kotlin.test.assertIs
import kotlin.test.fail

inline fun <reified E : Throwable> assertThrows(block: () -> Unit) {
    try {
        block()
        fail("Expected a ${E::class.simpleName} to be thrown")
    } catch (e: Throwable) {
        assertIs<E>(e)
    }
}