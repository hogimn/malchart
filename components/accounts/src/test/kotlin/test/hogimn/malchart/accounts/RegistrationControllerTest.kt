package test.hogimn.malchart.accounts

import com.hogimn.malchart.accounts.AccountDataGateway
import com.hogimn.malchart.accounts.RegistrationController
import com.hogimn.malchart.accounts.RegistrationService
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.jdbcsupport.TransactionManager
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import com.hogimn.malchart.users.UserDataGateway
import com.hogimn.malchart.users.UserInfo
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class RegistrationControllerTest : TestControllerSupport() {
    val dataSource = DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/registration_test?user=uservices&password=uservices")

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            val transactionManager = TransactionManager(dataSource)
            val template = JdbcTemplate(dataSource)
            context("/registration", RegistrationController(mapper, RegistrationService(transactionManager, UserDataGateway(template), AccountDataGateway(template))))
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
    fun testRegister() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val registrationResponse = template.post("http://localhost:8081/registration", "application/json", "{\"name\":\"aUser\"}")
        val actual = mapper.readValue(registrationResponse, UserInfo::class.java)

        assert(actual.id > 0)
        assertEquals("aUser", actual.name)
        assertEquals("registration info", actual.info)
    }
}
