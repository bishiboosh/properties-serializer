/*
 * MIT License
 *
 * Copyright (c) 2025 Valentin Rocher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.bishiboosh.properties.internal

import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Properties
import kotlin.test.assertEquals

class InteropTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSerialization() {
        val expected = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3",
            "key4" to "valuéééééé4"
        )
        val properties = Properties()
        for ((key, value) in expected) {
            properties.setProperty(key, value)
        }
        val file = tempFolder.newFile("test.properties")
        file.outputStream().use { properties.store(it, null) }
        val map = file.inputStream().asSource().buffered().use { it.commonReadPropertiesMap() }
        assertEquals(expected, map)
    }

    @Test
    fun testDeserialization() {
        val expected = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value3",
            "key4" to "valuéééééé4"
        )
        val file = tempFolder.newFile("test.properties")
        file.outputStream().asSink().buffered().use { it.commonWritePropertiesMap(expected) }
        val properties = file.inputStream().use { input ->
            Properties().apply { load(input) }
        }
        assertEquals(expected.size, properties.size)
        for ((key, value) in expected) {
            assertEquals(value, properties.getProperty(key))
        }
    }
}