package io.github.bishiboosh.properties.internal

import kotlinx.io.Source

private const val MODE_NONE = 0
private const val MODE_SLASH = 1
private const val MODE_UNICODE = 2
private const val MODE_CONTINUE = 3
private const val MODE_KEY_DONE = 4
private const val MODE_IGNORE = 5

private fun Source.read(): Int {
    if (exhausted()) return -1
    return readByte().toInt() and 0xff
}

internal fun Source.commonReadPropertiesMap(): Map<String, String> {
    var mode = MODE_NONE
    var unicode = 0
    var count = 0
    var nextChar = ' '
    var buf = CharArray(40)
    var offset = 0
    var keyLength = -1
    var intVal = 0
    var firstChar = true
    val result = mutableMapOf<String, String>()

    while (true) {
        intVal = read()
        if (intVal == -1) {
            // if mode is UNICODE but has less than 4 hex digits, should
            // throw an IllegalArgumentException
            require(mode != MODE_UNICODE || count >= 4) {
                "Invalid Unicode sequence: expected format \\uxxxx"
            }
            // if mode is SLASH and no data is read, should append '\u0000'
            // to buf
            if (mode == MODE_SLASH) buf[offset++] = '\u0000'
            break
        }
        nextChar = (intVal and 0xff).toChar()

        if (offset == buf.size) {
            val newBuf = CharArray(buf.size * 2)
            buf.copyInto(newBuf, 0, 0, offset)
            buf = newBuf
        }
        if (mode == MODE_UNICODE) {
            val digit = nextChar.digitToInt(16)
            if (digit >= 0) {
                unicode = (unicode shl 4) + digit
                if (++count < 4) continue
            } else if (count <= 4) {
                throw IllegalArgumentException("Invalid Unicode sequence: illegal character")
            }
            mode = MODE_NONE
            buf[offset++] = unicode.toChar()
            if (nextChar != '\n') continue
        }
        if (mode == MODE_SLASH) {
            mode = MODE_NONE
            when (nextChar) {
                '\r' -> {
                    mode = MODE_CONTINUE
                    continue
                }

                '\n' -> {
                    mode = MODE_IGNORE
                    continue
                }

                'b' -> nextChar = '\b'
                'f' -> nextChar = '\u000c'
                'n' -> nextChar = '\n'
                'r' -> nextChar = '\r'
                't' -> nextChar = '\t'
                'u' -> {
                    mode = MODE_UNICODE
                    unicode = 0
                    count = 0
                    continue
                }
            }
        } else {
            when (nextChar) {
                '#', '!' -> if (firstChar) {
                    while (true) {
                        intVal = read()
                        if (intVal == -1) break
                        nextChar = intVal.toChar()
                        if (nextChar == '\r' || nextChar == '\n') break
                    }
                    continue
                }

                '\n', '\r' -> {
                    if (nextChar == '\n' && mode == MODE_CONTINUE) { // Part of a \r\n sequence
                        mode = MODE_IGNORE // Ignore whitespace on the next line
                        continue
                    }
                    mode = MODE_NONE
                    firstChar = true
                    if (offset > 0 || (offset == 0 && keyLength == 0)) {
                        if (keyLength == -1) keyLength = offset
                        val tmp = buf.concatToString(0, offset)
                        result[tmp.substring(0, keyLength)] = tmp.substring(keyLength)
                    }
                    keyLength = -1
                    offset = 0
                    continue
                }

                '\\' -> {
                    if (mode == MODE_KEY_DONE) keyLength = offset
                    mode = MODE_SLASH
                    continue
                }

                ':', '=' -> if (keyLength == -1) { // if parsing the key
                    mode = MODE_NONE
                    keyLength = offset
                    continue
                }
            }
            if (nextChar.code < 256 && nextChar.isWhitespace()) {
                if (mode == MODE_CONTINUE) mode = MODE_IGNORE
                // if key length == 0 or value length == 0
                if (offset == 0 || offset == keyLength || mode == MODE_IGNORE) continue
                if (keyLength == -1) {
                    mode = MODE_KEY_DONE
                    continue
                }
            }
            if (mode == MODE_IGNORE || mode == MODE_CONTINUE) mode = MODE_NONE
        }
        firstChar = false
        if (mode == MODE_KEY_DONE) {
            keyLength = offset
            mode = MODE_NONE
        }
        buf[offset++] = nextChar
    }
    if (keyLength == -1 && offset > 0) keyLength = offset
    if (keyLength >= 0) {
        val tmp = buf.concatToString(0, offset)
        result[tmp.substring(0, keyLength)] = tmp.substring(keyLength)
    }
    return result
}