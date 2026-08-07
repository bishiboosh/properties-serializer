package io.github.bishiboosh.properties.model

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.asInputStream
import kotlinx.io.asOutputStream
import java.util.Hashtable
import java.util.Properties

public actual typealias PropertyContainer = Properties

/**
 * Creates a new property container with the specified key-value pairs.
 *
 * @param pairs the key-value pairs to include in the property container.
 * @return a new property container with the specified key-value pairs.
 */
public actual fun propertyContainerOf(vararg pairs: Pair<String, String>): PropertyContainer =
    Properties().apply {
        for ((key, value) in pairs) {
            setProperty(key, value)
        }
    }

/**
 * Copies all the mappings from the specified property container to this property container.
 * These mappings will replace any mappings that this property container had for any of the keys
 * currently in the specified property container.
 *
 * @param from the property container from which to copy mappings.
 */
public actual fun PropertyContainer.putAll(from: PropertyContainer) {
    this.putAll(from as Hashtable<*, *>)
}

/**
 * Returns a view of this property container as a map
 */
@Suppress("UNCHECKED_CAST") // We know keys and values are Strings in this context
public actual fun PropertyContainer.asMap(): MutableMap<String, String> {
    val hashTable = this as java.util.Hashtable<String, String>
    return object : MutableMap<String, String> by hashTable {}
}

/**
 * Reads a property container from the given [Source]. The source stays open after this function
 * returns, and it is the caller's responsibility to close it when done.
 *
 * @see Properties.load
 */
public actual fun Source.readPropertyContainer(): PropertyContainer = Properties().apply {
    load(asInputStream())
}

/**
 * Writes a property container to the given [Sink]. The sink stays open after this function returns,
 * and it is the caller's responsibility to close it when done.
 *
 * @see Properties.store
 */
public actual fun Sink.writePropertyContainer(propertyContainer: PropertyContainer) {
    propertyContainer.store(asOutputStream(), null)
}
