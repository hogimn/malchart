package test.com.hogimn.malchart.users

import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import com.hogimn.malchart.users.UserController
import com.hogimn.malchart.users.UserDataGateway
import com.hogimn.malchart.users.UserInfo
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class UserControllerTest : TestControllerSupport() {
    val dataSource =
        DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/registration_test?user=uservices&password=uservices")

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context("/users", UserController(mapper, UserDataGateway(JdbcTemplate(dataSource))))
        }
    }

    @Before
    fun setUp() {
        JdbcTemplate(dataSource).apply {
            execute("delete from projects")
            execute("delete from accounts")
            execute("delete from users")
        }
        server.start()
    }

    @After
    fun tearDown() {
        server.stop()
    }

    @Test
    fun testShow() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/users", "application/json", Pair("userId", "4765"))
        val actual = mapper.readValue(response, UserInfo::class.java)

        assertEquals(4765L, actual.id)
        assertEquals("Jack", actual.name)
        assertEquals("user info", actual.info)
    }
}
