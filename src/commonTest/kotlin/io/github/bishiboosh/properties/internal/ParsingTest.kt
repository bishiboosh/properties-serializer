package io.github.bishiboosh.properties.internal

import io.github.bishiboosh.properties.assertThrows
import io.github.bishiboosh.properties.model.PropertyContainer
import io.github.bishiboosh.properties.model.get
import io.github.bishiboosh.properties.model.propertyContainerOf
import io.github.bishiboosh.properties.model.readPropertyContainer
import io.github.bishiboosh.properties.model.writePropertyContainer
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ParsingTest {

    private fun read(value: String): PropertyContainer {
        return Buffer().apply { writeString(value) }.use { it.readPropertyContainer() }
    }

    private fun checkValue(expected: String, key: String, source: String) {
        assertEquals(expected, read(source).getProperty(key))
    }

    @Test
    fun testRead() {
        assertEquals(
            expected = propertyContainerOf(
                "test.pkg" to "tests",
                "test.proj" to "Tests"
            ),
            actual = read(TEST_PROPERTIES)
        )
        checkValue("", "", "=")
        checkValue("", "", "=\r\n")
        checkValue("", "", "=\n\r")
        checkValue("", "", " = ")
        checkValue("b", "a", " a= b")
        checkValue("b", "a", " a b")
        checkValue("value", "a", "#comment\na=value")
        checkValue("1", "fred", "#properties file\r\nfred=1\r\n#last comment")
        read("a=\\u1234z")
        assertThrows<IllegalArgumentException> { read("a=\\u123") }
        assertThrows<IllegalArgumentException> { read("a=\\u123z") }
        assertEquals(propertyContainerOf("a" to "q"), read("a=\\q"))
    }

    @Test
    fun testReadComplete() {
        val properties = read(SPECIAL_TEST_PROPERTIES)
        assertEquals("\n \t \u000c", properties[" \r"])
        assertEquals("a", properties["a"])
        assertEquals("bb as,dn   ", properties["b"])
        assertEquals(":: cu", properties["c\r \t\nu"])
        assertEquals("bu", properties["bu"])
        assertEquals("d\r\ne=e", properties["d"])
        assertEquals("fff", properties["f"])
        assertEquals("g", properties["g"])
        assertEquals("", properties["h h"])
        assertEquals("i=i", properties[" "])
        assertEquals("   j", properties["j"])
        assertEquals("   c", properties["space"])
        assertEquals("\\", properties["dblbackslash"])
    }

    @Test
    fun testWrite() {
        val properties = propertyContainerOf(
            "Property A" to "aye",
            "Property B" to "bee",
            "Property C" to "see",
        )
        val buffer = Buffer()
        buffer.writePropertyContainer(properties)
        val expectedLines = listOf(
            "Property\\ A=aye",
            "Property\\ B=bee",
            "Property\\ C=see"
        )
        val usefulLines = buffer
            .readString()
            .lines()
            .filterNot { it.startsWith('#') || it.isBlank() }
        assertEquals(expectedLines.size, usefulLines.size)
        for (expectedLine in expectedLines) {
            assertContains(usefulLines, expectedLine)
        }
    }

    @Test
    fun testRoundabout() {
        val properties = propertyContainerOf(
            "Property A" to "aye",
            "Property B" to "bee",
            "Property C" to "see",
        )
        val buffer = Buffer()
        buffer.writePropertyContainer(properties)
        assertEquals(properties, buffer.readPropertyContainer())
    }

    companion object {
        private val TEST_PROPERTIES = """
        #commented.entry=Bogus
        test.pkg=tests
        test.proj=Tests
        """.trimIndent()

        private val SPECIAL_TEST_PROPERTIES = """
        

    
    		
   \ \r \n \t \f
   
            					
! dshfjklahfjkldashgjl;as
     #jdfagdfjagkdjfghksdajfd
     
!!properties

a=a
b bb as,dn   
c\r\ \t\nu =:: cu
bu= b\
		u
d=d\r\ne=e
f   :f\
f\
			f
g		g
h\u0020h
\   i=i
j=\   j
space=\   c

dblbackslash=\\
                         
        """
    }
}
