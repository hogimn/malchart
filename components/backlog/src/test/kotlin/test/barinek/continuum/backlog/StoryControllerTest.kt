package test.barinek.continuum.backlog

import com.fasterxml.jackson.core.type.TypeReference
import io.barinek.continuum.backlog.ProjectClient
import io.barinek.continuum.backlog.ProjectInfo
import io.barinek.continuum.backlog.StoryController
import io.barinek.continuum.backlog.StoryDataGateway
import io.barinek.continuum.backlog.StoryInfo
import io.barinek.continuum.jdbcsupport.DataSourceConfig
import io.barinek.continuum.jdbcsupport.JdbcTemplate
import io.barinek.continuum.restsupport.BasicServer
import io.barinek.continuum.testsupport.TestControllerSupport
import io.barinek.continuum.testsupport.TestScenarioSupport
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class StoryControllerTest : TestControllerSupport() {
    val dataSource = DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/backlog_test?user=uservices&password=uservices")
    val client = mock<ProjectClient>()

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context("/stories", StoryController(mapper, StoryDataGateway(JdbcTemplate(dataSource)), client))
        }
    }

    @Before
    fun setUp() {
        JdbcTemplate(dataSource).apply {
            execute("delete from stories")
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

        whenever(client.getProject(any())).thenReturn(ProjectInfo(true))

        val json = "{\"projectId\":55432,\"name\":\"An epic story\"}"
        val response = template.post("http://localhost:8081/stories", "application/json", json)
        val actual = mapper.readValue(response, StoryInfo::class.java)

        assert(actual.id > 0)
        assertEquals(55432L, actual.projectId)
        assertEquals("An epic story", actual.name)
        assertEquals("story info", actual.info)
    }

    @Test
    fun testFailedCreate() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        whenever(client.getProject(any())).thenReturn(ProjectInfo(false))

        val json = "{\"projectId\":55432,\"name\":\"An epic story\"}"
        val response = template.post("http://localhost:8081/stories", "application/json", json)
        assertEquals("status_code 422", response)
    }

    @Test
    fun testFind() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/stories", "application/json", Pair("projectId", "55432"))
        val stories: List<StoryInfo> = mapper.readValue(response, object : TypeReference<List<StoryInfo>>() {})
        val actual = stories.first()

        assertEquals(876L, actual.id)
        assertEquals(55432L, actual.projectId)
        assertEquals("An epic story", actual.name)
        assertEquals("story info", actual.info)
    }
}
