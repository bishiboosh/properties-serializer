package io.github.bishiboosh.properties.internal

import io.github.bishiboosh.properties.assertThrows
import kotlinx.io.Buffer
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.test.Test
import kotlin.test.assertEquals

class ParsingTest {

    private fun read(value: String): Map<String, String> {
        return Buffer().apply { writeString(value) }.use { it.readPropertiesMap() }
    }

    private fun checkValue(expected: String, key: String, source: String) {
        assertEquals(expected, read(source)[key])
    }

    @Test
    fun testRead() {
        assertEquals(
            expected = mapOf(
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
        assertEquals(mapOf("a" to "q"), read("a=\\q"))
    }

    @Test
    fun testReadComplete() {
        val map = read(SPECIAL_TEST_PROPERTIES)
        assertEquals("\n \t \u000c", map[" \r"])
        assertEquals("a", map["a"])
        assertEquals("bb as,dn   ", map["b"])
        assertEquals(":: cu", map["c\r \t\nu"])
        assertEquals("bu", map["bu"])
        assertEquals("d\r\ne=e", map["d"])
        assertEquals("fff", map["f"])
        assertEquals("g", map["g"])
        assertEquals("", map["h h"])
        assertEquals("i=i", map[" "])
        assertEquals("   j", map["j"])
        assertEquals("   c", map["space"])
        assertEquals("\\", map["dblbackslash"])
    }

    @Test
    fun testWrite() {
        val map = mapOf(
            "Property A" to "aye",
            "Property B" to "bee",
            "Property C" to "see",
        )
        val buffer = Buffer()
        buffer.writePropertiesMap(map)
        assertEquals(
            expected = listOf(
                "Property\\ A=aye",
                "Property\\ B=bee",
                "Property\\ C=see"
            ),
            actual = buffer
                .readString()
                .lines()
                .filterNot { it.startsWith('#') || it.isBlank() }
        )
    }

    @Test
    fun testRoundabout() {
        val map = mapOf(
            "Property A" to "aye",
            "Property B" to "bee",
            "Property C" to "see",
        )
        val buffer = Buffer()
        buffer.writePropertiesMap(map)
        assertEquals(map, buffer.readPropertiesMap())
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