package test.hogimn.malchart.projects

import com.fasterxml.jackson.core.type.TypeReference
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.projects.ProjectControllerV2
import com.hogimn.malchart.projects.ProjectDataGateway
import com.hogimn.malchart.projects.ProjectInfoV2
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ProjectControllerV2Test : TestControllerSupport() {
    val dataSource = DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/registration_test?user=uservices&password=uservices")

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            val controller = ProjectControllerV2(mapper, ProjectDataGateway(JdbcTemplate(dataSource)))
            context("/projects", controller)
            context("/project", controller)
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
    fun testCreate() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val json = "{\"accountId\":1673,\"name\":\"aProject\",\"active\":true,\"funded\":true}"
        val response = template.post("http://localhost:8081/projects", "application/vnd.appcontinuum.v2+json", json)
        val actual = mapper.readValue(response, ProjectInfoV2::class.java)

        assertEquals(1673L, actual.accountId)
        assertEquals("aProject", actual.name)
        assertEquals("project info", actual.info)
        assert(actual.active)
        assert(actual.funded)
    }

    @Test
    fun testList() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/projects", "application/vnd.appcontinuum.v2+json", Pair("accountId", "1673"))
        val list: List<ProjectInfoV2> = mapper.readValue(response, object : TypeReference<List<ProjectInfoV2>>() {})
        val actual = list.first()

        assertEquals(55432L, actual.id)
        assertEquals(1673L, actual.accountId)
        assertEquals("Flagship", actual.name)
        assertEquals("project info", actual.info)
        assert(actual.active)
        assertFalse(actual.funded)
    }

    @Test
    fun testGet() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/project", "application/vnd.appcontinuum.v2+json", Pair("projectId", "55432"))
        val actual = mapper.readValue(response, ProjectInfoV2::class.java)

        assertEquals(55432L, actual.id)
        assertEquals(1673L, actual.accountId)
        assertEquals("Flagship", actual.name)
        assertEquals("project info", actual.info)
        assert(actual.active)
        assertFalse(actual.funded)
    }

    @Test
    fun testNotFound() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/project", "application/vnd.appcontinuum.v2+json", Pair("projectId", "5280"))
        assertEquals("status_code 422", response)
    }
}
