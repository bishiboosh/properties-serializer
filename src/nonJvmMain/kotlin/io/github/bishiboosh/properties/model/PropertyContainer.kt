package io.github.bishiboosh.properties.model

import io.github.bishiboosh.properties.internal.commonReadPropertiesMap
import io.github.bishiboosh.properties.internal.commonWritePropertiesMap
import kotlinx.io.Sink
import kotlinx.io.Source

/**
 * A container for properties, which are key-value pairs where both the key and the value are
 * strings.
 */
public actual class PropertyContainer internal constructor(
    internal val map: MutableMap<String, String>,
) {
    public actual constructor() : this(mutableMapOf())

    public actual constructor(initialCapacity: Int) : this(LinkedHashMap(initialCapacity))

    /**
     * Searches for the property with the specified key in this property list. The method returns
     * null if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     */
    public actual fun getProperty(key: String): String? = map[key]

    /**
     * Sets the property with the specified key and value in this property list. If the property
     * already exists, the old value is replaced by the specified value.
     *
     * @param key the property key.
     * @param value the property value.
     * @return the previous value of the specified key in this property list, or null if it did
     * not have one.
     */
    public actual fun setProperty(key: String, value: String): Any? = map.put(key, value)

    /**
     * Searches for the property with the specified key in this property list. The method returns
     * the default value argument if the property is not found.
     *
     * @param key the property key
     * @param defaultValue a default value.
     * @return the value in this property list with the specified key value.
     */
    public actual fun getProperty(key: String, defaultValue: String): String =
        getProperty(key) ?: defaultValue

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
    public actual fun stringPropertyNames(): Set<String> = map.keys.toSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PropertyContainer

        return map == other.map
    }

    override fun hashCode(): Int {
        return map.hashCode()
    }
}

/**
 * Creates a new property container with the specified key-value pairs.
 *
 * @param pairs the key-value pairs to include in the property container.
 * @return a new property container with the specified key-value pairs.
 */
public actual fun propertyContainerOf(vararg pairs: Pair<String, String>): PropertyContainer =
    PropertyContainer(mutableMapOf(pairs = pairs))

/**
 * Copies all the mappings from the specified property container to this property container.
 * These mappings will replace any mappings that this property container had for any of the keys
 * currently in the specified property container.
 *
 * @param from the property container from which to copy mappings.
 */
public actual fun PropertyContainer.putAll(from: PropertyContainer) {
    map.putAll(from.map)
}

/**
 * Returns a view of this property container as a [MutableMap]
 */
public actual fun PropertyContainer.asMap(): MutableMap<String, String> = map

/**
 * Reads a property container from the given [Source]. The source stays open after this function
 * returns, and it is the caller's responsibility to close it when done.
 */
public actual fun Source.readPropertyContainer(): PropertyContainer {
    return PropertyContainer(commonReadPropertiesMap())
}

/**
 * Writes a property container to the given [Sink]. The sink stays open after this function returns,
 * and it is the caller's responsibility to close it when done.
 */
public actual fun Sink.writePropertyContainer(propertyContainer: PropertyContainer) {
    commonWritePropertiesMap(propertyContainer.map)
}
