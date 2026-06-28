package com.hogimn.malchart.anime

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController
import kotlin.text.toLong

class AnimeController(val mapper: ObjectMapper, val gateway: AnimeDataGateway) : BasicController() {
    override fun handle(exchange: HttpExchange): Boolean {
        return get(exchange, "/anime", listOf("application/json", "application/vnd.malchart.v1+json")) {
            val animeId = parameters(exchange)["animeId"]!!
            val list = gateway.findBy(animeId.toLong()).map { record ->
                AnimeInfo(
                    id = record.id,
                    title = record.title,
                    link = record.link,
                    image = record.image,
                    score = record.score,
                    members = record.members,
                    genre = record.genre,
                    studios = record.studios,
                    source = record.source,
                    season = record.season,
                    year = record.year,
                    rank = record.rank,
                    popularity = record.popularity,
                    scoringCount = record.scoringCount,
                    episodes = record.episodes,
                    airStatus = record.airStatus,
                    type = record.type,
                    startDate = record.startDate,
                    endDate = record.endDate,
                    englishTitle = record.englishTitle,
                    japaneseTitle = record.japaneseTitle,
                    synopsis = record.synopsis,
                    createdAt = record.createdAt,
                    updatedAt = record.updatedAt,
                    largeImage = record.largeImage,
                    rating = record.rating,
                    nsfw = record.nsfw,
                    "anime info"
                )
            }
            mapper.writeValueAsString(list)
        }
    }
}
