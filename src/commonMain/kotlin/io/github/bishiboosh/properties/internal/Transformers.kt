package io.github.bishiboosh.properties.internal

import kotlinx.io.Sink
import kotlinx.io.Source

internal expect fun Source.readPropertiesMap(): Map<String, String>

internal expect fun Sink.writePropertiesMap(properties: Map<String, String>)