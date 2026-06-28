package test.hogimn.malchart.accounts

import com.hogimn.malchart.accounts.AccountDataGateway
import com.hogimn.malchart.accounts.RegistrationService
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.jdbcsupport.TransactionManager
import com.hogimn.malchart.users.UserDataGateway
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class RegistrationServiceTest {
    val dataSource = DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/registration_test?user=uservices&password=uservices")

    @Before
    fun cleanDatabase() {
        JdbcTemplate(dataSource).apply {
            execute("delete from projects")
            execute("delete from accounts")
            execute("delete from users")
        }
    }

    @Test
    fun testCreateUserWithAccount() {
        val template = JdbcTemplate(dataSource)
        val transactionManager = TransactionManager(dataSource)

        val usersGateway = UserDataGateway(template)
        val accountsGateway = AccountDataGateway(template)

        val service = RegistrationService(transactionManager, usersGateway, accountsGateway)
        val aUser = service.createUserWithAccount("aUser")

        assertEquals("aUser", aUser.name)
    }
}
