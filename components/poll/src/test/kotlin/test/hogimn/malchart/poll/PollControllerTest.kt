package test.hogimn.malchart.poll

import com.fasterxml.jackson.core.type.TypeReference
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.poll.PollController
import com.hogimn.malchart.poll.PollDataGateway
import com.hogimn.malchart.poll.PollInfo
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class PollControllerTest : TestControllerSupport() {
    val dataSource =
        DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/poll_test?user=uservices&password=uservices")

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context(
                "/poll",
                PollController(
                    mapper,
                    PollDataGateway(JdbcTemplate(dataSource))
                )
            )
        }
    }

    @Before
    fun setUp() {
        JdbcTemplate(dataSource).apply {
            execute("delete from poll")
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

        val response = template.get(
            "http://localhost:8081/poll",
            "application/json",
            Pair("contentId", "4765"),
            Pair("topicId", "101"),
            Pair("pollOptionId", "1")
        )
        val actual: PollInfo = mapper.readValue(response, object : TypeReference<PollInfo>() {})

        assertEquals(4765, actual.contentId)
        assertEquals(101, actual.topicId)
        assertEquals(1, actual.pollOptionId)
        assertEquals("To You, in 2000 Years: The Fall of Shiganshina, Part 1", actual.title)
        assertEquals(1, actual.episode)
        assertEquals(15240, actual.votes)
        assertEquals(LocalDateTime.of(2026, 6, 28, 21, 0, 0), actual.createdAt)
        assertEquals(LocalDateTime.of(2026, 6, 28, 21, 0, 0), actual.updatedAt)
    }
}