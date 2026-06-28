package test.hogimn.malchart.accounts

import com.fasterxml.jackson.core.type.TypeReference
import com.hogimn.malchart.accounts.AccountController
import com.hogimn.malchart.accounts.AccountDataGateway
import com.hogimn.malchart.accounts.AccountInfo
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AccountControllerTest : TestControllerSupport() {
    val dataSource = DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/registration_test?user=uservices&password=uservices")

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context("/accounts", AccountController(mapper, AccountDataGateway(JdbcTemplate(dataSource))))
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
    fun testFind() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val ownerId = Pair("ownerId", "4765")
        val response = template.get("http://localhost:8081/accounts", "application/json", ownerId)
        val list: List<AccountInfo> = mapper.readValue(response, object : TypeReference<List<AccountInfo>>() {})
        val actual = list.first()

        assertEquals(1673L, actual.id)
        assertEquals(4765L, actual.ownerId)
        assertEquals("Jack's account", actual.name)
        assertEquals("account info", actual.info)
    }
}
