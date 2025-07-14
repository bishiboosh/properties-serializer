package io.github.bishiboosh.properties.internal

import io.github.bishiboosh.properties.Properties
import io.github.bishiboosh.properties.decodeFromByteArray
import io.github.bishiboosh.properties.decodeFromSource
import io.github.bishiboosh.properties.decodeFromString
import io.github.bishiboosh.properties.encodeToByteArray
import io.github.bishiboosh.properties.encodeToSink
import io.github.bishiboosh.properties.encodeToString
import kotlinx.io.Buffer
import kotlinx.io.readLine
import kotlinx.io.writeString
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class SerializationTest {

    @Test
    fun testDeserialize() {
        val expected = GradleProperties(
            distributionBase = "GRADLE_USER_HOME",
            distributionPath = "wrapper/dists",
            distributionUrl = "https://services.gradle.org/distributions/gradle-9.0.0-rc-1-bin.zip",
            zipStoreBase = "GRADLE_USER_HOME",
            zipStorePath = "wrapper/dists"
        )
        assertEquals(expected, Properties.decodeFromString(PROPERTIES_FILE))
        val source = Buffer().apply { writeString(PROPERTIES_FILE) }
        assertEquals(expected, Properties.decodeFromSource(source))
        assertEquals(expected, Properties.decodeFromByteArray(PROPERTIES_FILE.encodeToByteArray()))
    }

    private fun testSerializedLines(lines: List<String>) {
        val expectedLines = listOf(
            "distributionBase=GRADLE_USER_HOME",
            "distributionPath=wrapper/dists",
            "distributionUrl=https\\://services.gradle.org/distributions/gradle-9.0.0-rc-1-bin.zip",
            "zipStoreBase=GRADLE_USER_HOME",
            "zipStorePath=wrapper/dists"
        )
        val usefulLines = lines.filterNot { it.startsWith('#') || it.isBlank() }
        assertEquals(expectedLines.size, usefulLines.size)
        for (line in expectedLines) {
            assertContains(usefulLines, line)
        }
    }

    @Test
    fun testSerialize() {
        val properties = GradleProperties(
            distributionBase = "GRADLE_USER_HOME",
            distributionPath = "wrapper/dists",
            distributionUrl = "https://services.gradle.org/distributions/gradle-9.0.0-rc-1-bin.zip",
            zipStoreBase = "GRADLE_USER_HOME",
            zipStorePath = "wrapper/dists"
        )
        val sink = Buffer()
        Properties.encodeToSink(sink, properties)
        var line = sink.readLine()
        val lines = mutableListOf<String>()
        while (line != null) {
            lines.add(line)
            line = sink.readLine()
        }
        testSerializedLines(lines)
        testSerializedLines(Properties.encodeToString(properties).lines())
        testSerializedLines(Properties.encodeToByteArray(properties).decodeToString().lines())
    }

    @Serializable
    private data class GradleProperties(
        val distributionBase: String,
        val distributionPath: String,
        val distributionUrl: String,
        val zipStoreBase: String,
        val zipStorePath: String
    )

    companion object {
        private val PROPERTIES_FILE = """
        #Fri Jul 11 11:11:51 CEST 2025
        distributionBase=GRADLE_USER_HOME
        distributionPath=wrapper/dists
        distributionUrl=https\://services.gradle.org/distributions/gradle-9.0.0-rc-1-bin.zip
        zipStoreBase=GRADLE_USER_HOME
        zipStorePath=wrapper/dists
    
        """.trimIndent()
    }
}