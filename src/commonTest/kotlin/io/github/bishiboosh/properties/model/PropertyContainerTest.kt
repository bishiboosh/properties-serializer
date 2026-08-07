package io.github.bishiboosh.properties.model

import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyContainerTest {

    @Test
    fun testClassic() {
        val propertyContainer = PropertyContainer()
        propertyContainer.setProperty("key1", "value1")
        assertEquals("value1", propertyContainer.getProperty("key1"))
    }

    @Test
    fun testReplace() {
        val propertyContainer = PropertyContainer()
        propertyContainer.setProperty("key1", "value1")
        propertyContainer.setProperty("key1", "value2")
        assertEquals("value2", propertyContainer.getProperty("key1"))
    }

    @Test
    fun testDefault() {
        val propertyContainer = PropertyContainer()
        assertEquals("default", propertyContainer.getProperty("key1", "default"))
    }

    @Test
    fun testStringPropertyNames() {
        val propertyContainer = PropertyContainer()
        propertyContainer.setProperty("key1", "value1")
        propertyContainer.setProperty("key2", "value2")
        val keys = propertyContainer.stringPropertyNames()
        assertEquals(setOf("key1", "key2"), keys)
    }

    @Test
    fun testOperators() {
        val propertyContainer = PropertyContainer()
        propertyContainer["key1"] = "value1"
        assertEquals("value1", propertyContainer["key1"])
    }

    @Test
    fun testBuilder() {
        val propertyContainer = propertyContainerOf(
            "key1" to "value1",
            "key2" to "value2"
        )
        assertEquals("value1", propertyContainer.getProperty("key1"))
        assertEquals("value2", propertyContainer.getProperty("key2"))
    }

    @Test
    fun testPutAll() {
        val propertyContainer1 = propertyContainerOf(
            "key1" to "value1",
            "key2" to "value2"
        )
        val propertyContainer2 = propertyContainerOf(
            "key3" to "value3"
        )
        propertyContainer1.putAll(propertyContainer2)
        assertEquals("value1", propertyContainer1.getProperty("key1"))
        assertEquals("value2", propertyContainer1.getProperty("key2"))
        assertEquals("value3", propertyContainer1.getProperty("key3"))
    }

    @Test
    fun testAsMap() {
        val propertyContainer = propertyContainerOf(
            "key1" to "value1",
            "key2" to "value2"
        )
        val map = propertyContainer.asMap()
        assertEquals(mapOf("key1" to "value1", "key2" to "value2"), map)
        map["key3"] = "value3"
        assertEquals("value3", propertyContainer.getProperty("key3"))
    }
}
