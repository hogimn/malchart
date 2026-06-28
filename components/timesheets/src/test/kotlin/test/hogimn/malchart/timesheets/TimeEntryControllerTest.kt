package test.hogimn.malchart.timesheets

import com.fasterxml.jackson.core.type.TypeReference
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import com.hogimn.malchart.timesheets.ProjectClient
import com.hogimn.malchart.timesheets.ProjectInfo
import com.hogimn.malchart.timesheets.TimeEntryController
import com.hogimn.malchart.timesheets.TimeEntryDataGateway
import com.hogimn.malchart.timesheets.TimeEntryInfo
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDate
import kotlin.test.assertEquals

class TimeEntryControllerTest : TestControllerSupport() {
    val dataSource = DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/timesheets_test?user=uservices&password=uservices")
    val client = mock<ProjectClient>()

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context("/time-entries", TimeEntryController(mapper, TimeEntryDataGateway(JdbcTemplate(dataSource)), client))
        }
    }

    @Before
    fun setUp() {
        JdbcTemplate(dataSource).apply {
            execute("delete from time_entries")
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

        whenever(client.getProject(any())).thenReturn(ProjectInfo(active = true, funded = true))

        val json = "{\"projectId\":55432,\"userId\":4765,\"date\":\"2015-05-17\",\"hours\":8}"
        val response = template.post("http://localhost:8081/time-entries", "application/json", json)

        val actual = mapper.readValue(response, TimeEntryInfo::class.java)

        assert(actual.id > 0)
        assertEquals(55432L, actual.projectId)
        assertEquals(4765L, actual.userId)
        assertEquals(8, actual.hours)
    }

    @Test
    fun testFailedCreate() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        whenever(client.getProject(any())).thenReturn(ProjectInfo(active = true, funded = false))

        val json = "{\"projectId\":55432,\"userId\":4765,\"date\":\"2015-05-17\",\"hours\":8}"
        val response = template.post("http://localhost:8081/time-entries", "application/json", json)
        assertEquals("status_code 422", response)
    }

    @Test
    fun testFind() {
        TestScenarioSupport(dataSource).loadTestScenario("jacks-test-scenario")

        val response = template.get("http://localhost:8081/time-entries", "application/json", Pair("userId", "4765"))
        val stories: List<TimeEntryInfo> = mapper.readValue(response, object : TypeReference<List<TimeEntryInfo>>() {})
        val actual = stories.first()

        assertEquals(1534L, actual.id)
        assertEquals(55432L, actual.projectId)
        assertEquals(4765L, actual.userId)
        assertEquals(LocalDate.of(2015, 5, 17), actual.date)
        assertEquals(5, actual.hours)
    }
}
