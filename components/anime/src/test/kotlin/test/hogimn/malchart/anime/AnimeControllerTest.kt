package test.hogimn.malchart.anime

import com.fasterxml.jackson.core.type.TypeReference
import com.hogimn.malchart.anime.AnimeController
import com.hogimn.malchart.anime.AnimeDataGateway
import com.hogimn.malchart.anime.AnimeInfo
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.testsupport.TestControllerSupport
import com.hogimn.malchart.testsupport.TestScenarioSupport
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class AnimeControllerTest : TestControllerSupport() {
    val dataSource =
        DataSourceConfig().createDataSource("jdbc:mysql://localhost:3306/anime_test?user=uservices&password=uservices")

    private val server = object : BasicServer(8081) {
        override fun registerContexts() {
            context(
                "/anime",
                AnimeController(
                    mapper,
                    AnimeDataGateway(JdbcTemplate(dataSource))
                )
            )
        }
    }

    @Before
    fun setUp() {
        JdbcTemplate(dataSource).apply {
            execute("delete from anime")
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

        val id = Pair("animeId", "4765")
        val response = template.get("http://localhost:8081/anime", "application/json", id)
        val actual: AnimeInfo = mapper.readValue(response, object : TypeReference<AnimeInfo>() {})

        assertEquals(4765, actual.id)
        assertEquals("Attack on Titan", actual.title)
        assertEquals("https://example.com/anime/4765", actual.link)
        assertEquals("https://example.com/img/4765.jpg", actual.image)
        assertEquals(9.12, actual.score)
        assertEquals(2500000, actual.members)
        assertEquals("Action, Fantasy", actual.genre)
        assertEquals("WIT Studio", actual.studios)
        assertEquals("Manga", actual.source)
        assertEquals("SPRING", actual.season)
        assertEquals(2013, actual.year)
        assertEquals(2, actual.rank)
        assertEquals(1, actual.popularity)
        assertEquals(1800000, actual.scoringCount)
        assertEquals(25, actual.episodes)
        assertEquals("Finished Airing", actual.airStatus)
        assertEquals("TV", actual.type)
        assertEquals(LocalDateTime.of(2013, 4, 7, 0, 0, 0), actual.startDate)
        assertEquals(LocalDateTime.of(2013, 9, 29, 0, 0, 0), actual.endDate)
        assertEquals("Attack on Titan", actual.englishTitle)
        assertEquals("Shingeki no Kyojin", actual.japaneseTitle)
        assertEquals(
            "Centuries ago, mankind was slaughtered to near extinction by monstrous humanoid creatures called Titans...",
            actual.synopsis
        )
        assertEquals(LocalDateTime.of(2026, 6, 28, 21, 0, 0), actual.createdAt)
        assertEquals(LocalDateTime.of(2026, 6, 28, 21, 0, 0), actual.updatedAt)
        assertEquals("https://example.com/img/4765_large.jpg", actual.largeImage)
        assertEquals("R - 17+", actual.rating)
        assertEquals("SFW", actual.nsfw)
        assertEquals("anime info", actual.info)
    }
}