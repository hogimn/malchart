package test.barinek.continuum.redissupport

import io.barinek.continuum.redissupport.RedisConfig
import org.junit.Test
import kotlin.test.assertEquals

class RedisConfigTest {
    val client = RedisConfig().getClient("localhost", "foobared")

    @Test
    fun testFind() {
        client.set("aKey", "aValue")
        assertEquals("aValue", client.get("aKey"))
    }
}
