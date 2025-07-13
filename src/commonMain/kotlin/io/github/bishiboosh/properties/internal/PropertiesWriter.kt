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
 *
 * This file incorporates work covered by the following copyright and permission notice:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.bishiboosh.properties.internal

import kotlinx.io.Sink
import kotlinx.io.writeString

// Adapted for Apache Harmony

internal fun Sink.commonWritePropertiesMap(properties: Map<String, String>) {
    val buffer = StringBuilder(200)
    for ((key, value) in properties) {
        buffer.dumpString(key, true)
        buffer.append('=')
        buffer.dumpString(value, false)
        buffer.appendLine()
        writeString(buffer.toString())
        buffer.setLength(0)
    }
}

@Suppress("CyclomaticComplexMethod")
private fun StringBuilder.dumpString(string: String, isKey: Boolean) {
    var index = 0
    val length = string.length
    if (!isKey && index < length && string[index] == ' ') {
        append("\\ ")
        index++
    }
    while (index < length) {
        val ch = string[index]
        when (ch) {
            '\t' -> append("\\t")
            '\n' -> append("\\n")
            '\u000c' -> append("\\f")
            '\r' -> append("\\r")
            else -> {
                if ("\\#!=:".indexOf(ch) >= 0 || isKey && ch == ' ') {
                    append('\\')
                }
                if (ch >= ' ' && ch <= '~') {
                    append(ch)
                } else {
                    append(ch.toHexaDecimal())
                }
            }
        }
        index++
    }
}

private fun Char.toHexaDecimal(): CharArray {
    val hexChars = charArrayOf('\\', 'u', '0', '0', '0', '0')
    var hexChar: Int
    var index = hexChars.size
    var ch = code
    do {
        hexChar = ch and 15
        if (hexChar > 9) {
            hexChar = hexChar - 10 + 'A'.code
        } else {
            hexChar += '0'.code
        }
        hexChars[--index] = hexChar.toChar()
        ch = ch ushr 4
    } while (ch != 0)
    return hexChars
}