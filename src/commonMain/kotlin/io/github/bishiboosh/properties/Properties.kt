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
 * Copyright 2017-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
@file:Suppress("TooManyFunctions") // Entry point

package io.github.bishiboosh.properties

import io.github.bishiboosh.properties.internal.readPropertiesMap
import io.github.bishiboosh.properties.internal.writePropertiesMap
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.Source
import kotlinx.io.readByteArray
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.Contextual
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.SerializationException
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.internal.NamedValueDecoder
import kotlinx.serialization.internal.NamedValueEncoder
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer

/**
 * Transforms a [Serializable] class' properties into a format compatible with the one generated by
 * [Java Properties](https://docs.oracle.com/javase/8/docs/api/java/util/Properties.html).
 *
 * The properties are stored in a [Map] with string keys and values, serializaed with the format used
 * in Java Properties.
 *
 * If the given class has non-primitive property `d` of arbitrary type `D`, `D` values are inserted
 * into the same map; keys for such values are prefixed with string `d.`:
 *
 * ```
 * @Serializable
 * class Data(val property1: String)
 *
 * @Serializable
 * class DataHolder(val data: Data, val property2: String)
 *
 * val serialized = Properties.encodeToString(DataHolder(Data("value1"), "value2"))
 * // result will contain the following:
 * // property2=value2
 * // data.property1=value1
 * ```
 *
 * If the given class has a [List] property `l`, each value from the list
 * would be prefixed with `l.N.`, where N is an index for a particular value.
 * [Map] is treated as a `[key,value,...]` list.
 *
 * @param serializersModule A [SerializersModule] which should contain registered serializers
 * for [Contextual] and [Polymorphic] serialization, if you have any.
 */
@Suppress("UNUSED_PARAMETER")
public sealed class Properties(
    override val serializersModule: SerializersModule,
    ctorMarker: Nothing?
) : SerialFormat {

    @OptIn(InternalSerializationApi::class)
    private inner class OutMapper : NamedValueEncoder() {
        override val serializersModule: SerializersModule = this@Properties.serializersModule

        val map: MutableMap<String, String> = mutableMapOf()

        private fun encode(value: Any): String = value.toString()

        @Suppress("UNCHECKED_CAST")
        override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
            if (serializer is AbstractPolymorphicSerializer<*>) {
                val casted = serializer as AbstractPolymorphicSerializer<Any>
                val actualSerializer = casted.findPolymorphicSerializer(this, value as Any)
                encodeTaggedString(nested("type"), actualSerializer.descriptor.serialName)

                return actualSerializer.serialize(this, value)
            }

            return serializer.serialize(this, value)
        }

        override fun encodeTaggedValue(tag: String, value: Any) {
            map[tag] = encode(value)
        }

        override fun encodeTaggedNull(tag: String) {
            // ignore nulls in output
        }

        override fun encodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor, ordinal: Int) {
            map[tag] = encode(enumDescriptor.getElementName(ordinal))
        }
    }

    @OptIn(InternalSerializationApi::class)
    private inner class InMapper(
        private val map: Map<String, String>, descriptor: SerialDescriptor
    ) : NamedValueDecoder() {
        override val serializersModule: SerializersModule = this@Properties.serializersModule

        private var currentIndex = 0
        private val isCollection =
            descriptor.kind == StructureKind.LIST || descriptor.kind == StructureKind.MAP
        private val size = if (isCollection) Int.MAX_VALUE else descriptor.elementsCount

        private fun structure(descriptor: SerialDescriptor): InMapper = InMapper(map, descriptor)

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            return structure(descriptor).also { copyTagsTo(it) }
        }

        override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
            if (deserializer is AbstractPolymorphicSerializer<*>) {
                val type = map[nested("type")]
                val actualSerializer: DeserializationStrategy<Any> =
                    deserializer.findPolymorphicSerializer(this, type)

                @Suppress("UNCHECKED_CAST")
                return actualSerializer.deserialize(this) as T
            }

            return deserializer.deserialize(this)
        }

        override fun decodeTaggedValue(tag: String): String {
            return map.getValue(tag)
        }

        override fun decodeTaggedEnum(tag: String, enumDescriptor: SerialDescriptor): Int {
            val taggedValue = map.getValue(tag)
            return enumDescriptor.getElementIndex(taggedValue)
                .also {
                    if (it == CompositeDecoder.Companion.UNKNOWN_NAME) throw SerializationException(
                        "Enum '${enumDescriptor.serialName}' does not contain element with name '$taggedValue'"
                    )
                }
        }

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            while (currentIndex < size) {
                val name = descriptor.getTag(currentIndex++)
                if (map.keys.any {
                        it.startsWith(name) && (it.length == name.length || it[name.length] == '.')
                    }) return currentIndex - 1
                if (isCollection) {
                    // if map does not contain key we look for, then indices in collection have ended
                    break
                }
            }
            return CompositeDecoder.Companion.DECODE_DONE
        }

        override fun decodeTaggedBoolean(tag: String): Boolean = decodeTaggedValue(tag).toBoolean()
        override fun decodeTaggedByte(tag: String): Byte = decodeTaggedValue(tag).toByte()
        override fun decodeTaggedShort(tag: String): Short = decodeTaggedValue(tag).toShort()
        override fun decodeTaggedInt(tag: String): Int = decodeTaggedValue(tag).toInt()
        override fun decodeTaggedLong(tag: String): Long = decodeTaggedValue(tag).toLong()
        override fun decodeTaggedFloat(tag: String): Float = decodeTaggedValue(tag).toFloat()
        override fun decodeTaggedDouble(tag: String): Double = decodeTaggedValue(tag).toDouble()
        override fun decodeTaggedChar(tag: String): Char = decodeTaggedValue(tag).single()
    }

    private fun <T> encodeToMap(
        serializer: SerializationStrategy<T>,
        value: T
    ): Map<String, String> {
        val m = OutMapper()
        m.encodeSerializableValue(serializer, value)
        return m.map
    }

    /**
     * Encodes properties from the given [value] to a [Sink] using the provided [serializer].
     */
    public fun <T> encodeToSink(
        sink: Sink,
        serializer: SerializationStrategy<T>,
        value: T,
    ) {
        val map = encodeToMap(serializer, value)
        sink.writePropertiesMap(map)
    }

    private fun <T> decodeFromMap(
        deserializer: DeserializationStrategy<T>,
        map: Map<String, String>
    ): T {
        val m = InMapper(map, deserializer.descriptor)
        return m.decodeSerializableValue(deserializer)
    }

    /**
     * Decodes properties from the [Source] to a value of type [T] using the given [deserializer].
     */
    public fun <T> decodeFromSource(source: Source, deserializer: DeserializationStrategy<T>): T {
        val map = source.readPropertiesMap()
        return decodeFromMap(deserializer, map)
    }

    /**
     * A [Properties] instance that can be used as default and does not have any [SerializersModule] installed.
     */
    public companion object Default : Properties(EmptySerializersModule(), null)
}

private class PropertiesImpl(serializersModule: SerializersModule) :
    Properties(serializersModule, null)

/**
 * Creates an instance of [Properties] with a given [module].
 */
public fun Properties(module: SerializersModule): Properties = PropertiesImpl(module)

/**
 * Encodes properties from the given [value] to a [Sink] using serializer for reified type [T].
 */
public inline fun <reified T> Properties.encodeToSink(sink: Sink, value: T) {
    encodeToSink(sink, serializersModule.serializer(), value)
}

/**
 * Encodes properties from the given [value] to a [String] using the provided [serializer].
 */
public fun <T> Properties.encodeToString(
    serializer: SerializationStrategy<T>,
    value: T
): String {
    val sink = Buffer()
    encodeToSink(sink, serializer, value)
    return sink.readString()
}

/**
 * Encodes properties from the given [value] to a [String] using serializer for reified type [T].
 */
public inline fun <reified T> Properties.encodeToString(value: T): String {
    return encodeToString(serializersModule.serializer(), value)
}

/**
 * Encodes properties from the given [value] to a [ByteArray] using the provided [serializer].
 */
public fun <T> Properties.encodeToByteArray(
    serializer: SerializationStrategy<T>,
    value: T
): ByteArray {
    val sink = Buffer()
    encodeToSink(sink, serializer, value)
    return sink.readByteArray()
}

/**
 * Encodes properties from the given [value] to a [ByteArray] using serializer for reified type [T].
 */
public inline fun <reified T> Properties.encodeToByteArray(value: T): ByteArray {
    return encodeToByteArray(serializersModule.serializer(), value)
}

/**
 * Decodes properties from the [Source] to a value of type [T] using the deserializer for reified type [T].
 */
public inline fun <reified T> Properties.decodeFromSource(source: Source): T {
    return decodeFromSource(source, serializersModule.serializer())
}

/**
 * Decodes properties from the [String] to a value of type [T] using the given [deserializer].
 */
public fun <T> Properties.decodeFromString(
    string: String,
    deserializer: DeserializationStrategy<T>,
): T {
    val source = Buffer().apply { writeString(string) }
    return decodeFromSource(source, deserializer)
}

/**
 * Decodes properties from the [String] to a value of type [T] using the deserializer for reified type [T].
 */
public inline fun <reified T> Properties.decodeFromString(string: String): T {
    return decodeFromString(string, serializersModule.serializer())
}

/**
 * Decodes properties from the [ByteArray] to a value of type [T] using the given [deserializer].
 */
public fun <T> Properties.decodeFromByteArray(
    byteArray: ByteArray,
    deserializer: DeserializationStrategy<T>,
): T {
    val source = Buffer().apply { write(byteArray) }
    return decodeFromSource(source, deserializer)
}

/**
 * Decodes properties from the [ByteArray] to a value of type [T] using the deserializer for reified type [T].
 */
public inline fun <reified T> Properties.decodeFromByteArray(byteArray: ByteArray): T {
    return decodeFromByteArray(byteArray, serializersModule.serializer())
}