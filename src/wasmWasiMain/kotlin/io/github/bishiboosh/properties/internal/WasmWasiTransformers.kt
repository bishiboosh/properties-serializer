package io.github.bishiboosh.properties.internal

import kotlinx.io.Sink
import kotlinx.io.Source

internal actual fun Source.readPropertiesMap(): Map<String, String> = commonReadPropertiesMap()

internal actual fun Sink.writePropertiesMap(properties: Map<String, String>) {
    commonWritePropertiesMap(properties)
}