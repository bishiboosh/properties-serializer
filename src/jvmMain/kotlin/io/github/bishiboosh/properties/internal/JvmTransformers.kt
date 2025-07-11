package io.github.bishiboosh.properties.internal

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import java.util.Properties

internal actual fun Source.readPropertiesMap(): Map<String, String> {
    val properties = Properties().apply { load(asInputStream()) }
    return buildMap {
        for ((key, value) in properties) {
            put(key.toString(), value.toString())
        }
    }
}

internal actual fun Sink.writePropertiesMap(properties: Map<String, String>) {
    val props = Properties().apply {
        for ((key, value) in properties) {
            setProperty(key, value)
        }
    }
    props.store(asOutputStream(), null)
}