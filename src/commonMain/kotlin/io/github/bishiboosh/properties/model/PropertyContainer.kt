@file:JvmName("PropertyContainerUtils")

package io.github.bishiboosh.properties.model

import kotlinx.io.Sink
import kotlinx.io.Source
import kotlin.jvm.JvmName

/**
 * A container for properties, which are key-value pairs where both the key and the value are
 * strings.
 */
public expect class PropertyContainer {

    public constructor()

    public constructor(initialCapacity: Int)

    /**
     * Searches for the property with the specified key in this property list. The method returns
     * null if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     */
    public fun getProperty(key: String): String?

    /**
     * Sets the property with the specified key and value in this property list. If the property
     * already exists, the old value is replaced by the specified value.
     *
     * @param key the property key.
     * @param value the property value.
     * @return the previous value of the specified key in this property list, or null if it did
     * not have one.
     */
    public fun setProperty(key: String, value: String): Any?

    /**
     * Searches for the property with the specified key in this property list. The method returns
     * the default value argument if the property is not found.
     *
     * @param key the property key
     * @param defaultValue a default value.
     * @return the value in this property list with the specified key value.
     */
    public fun getProperty(key: String, defaultValue: String): String

    /**
     * Returns an unmodifiable set of keys in this property list where the key and its corresponding
     * value are strings.
     *
     * The returned set is not backed by this PropertyContainer object. Changes to this
     * PropertyContainer object are not reflected in the returned set.
     *
     * @return an unmodifiable set of keys in this property list where the key and its corresponding
     * value are strings
     */
    public fun stringPropertyNames(): Set<String>
}

/**
 * Get operator
 */
public operator fun PropertyContainer.get(key: String): String? = getProperty(key)

/**
 * Set operator
 */
public operator fun PropertyContainer.set(key: String, value: String) {
    setProperty(key, value)
}

/**
 * Creates a new property container with the specified key-value pairs.
 *
 * @param pairs the key-value pairs to include in the property container.
 * @return a new property container with the specified key-value pairs.
 */
public expect fun propertyContainerOf(vararg pairs: Pair<String, String>): PropertyContainer

/**
 * Copies all the mappings from the specified property container to this property container.
 * These mappings will replace any mappings that this property container had for any of the keys
 * currently in the specified property container.
 *
 * @param from the property container from which to copy mappings.
 */
public expect fun PropertyContainer.putAll(from: PropertyContainer)

/**
 * Returns a view of this property container as a [MutableMap]
 */
public expect fun PropertyContainer.asMap(): MutableMap<String, String>

/**
 * Reads a property container from the given [Source]. The source stays open after this function
 * returns, and it is the caller's responsibility to close it when done.
 */
public expect fun Source.readPropertyContainer(): PropertyContainer

/**
 * Writes a property container to the given [Sink]. The sink stays open after this function returns,
 * and it is the caller's responsibility to close it when done.
 */
public expect fun Sink.writePropertyContainer(propertyContainer: PropertyContainer)
