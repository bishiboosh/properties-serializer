package io.github.bishiboosh.properties.internal

import kotlinx.io.Sink
import kotlinx.io.writeString

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
                if ("\\#!=:".indexOf(ch) >= 0 || (isKey && ch == ' ')) {
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