package test.barinek.continuum.restsupport

import io.barinek.continuum.restsupport.BasicServer
import io.barinek.continuum.restsupport.DefaultController
import io.barinek.continuum.restsupport.RestTemplate
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DefaultControllerTest {
    val template = RestTemplate()

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context("/", DefaultController())
        }
    }

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.stop()
    }

    @Test
    fun testGet() {
        val response = template.get("http://localhost:8081/", "*/*")
        assertEquals("Noop!", response)
    }
}
