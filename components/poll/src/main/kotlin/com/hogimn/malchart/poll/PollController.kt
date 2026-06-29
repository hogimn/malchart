package com.hogimn.malchart.poll

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController

class PollController(val mapper: ObjectMapper, val gateway: PollDataGateway) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return get(exchange, "/poll", listOf("application/json", "application/vnd.malchart.v1+json")) {
            val id = parameters(exchange)["contentId"]!!
            val topicId = parameters(exchange)["topicId"]!!
            val pollOptionId = parameters(exchange)["pollOptionId"]!!
            val record = gateway.findObject(id.toInt(), topicId.toInt(), pollOptionId.toInt())
            if (record != null) {
                mapper.writeValueAsString(
                    PollInfo(
                        record.contentId,
                        record.topicId,
                        record.pollOptionId,
                        record.title,
                        record.episode,
                        record.votes,
                        record.createdAt,
                        record.updatedAt,
                        "poll info"
                    )
                )
            } else {
                throw IllegalStateException("Poll with id $id pollOptionId $pollOptionId topicId $topicId not found")
            }
        }
    }
}
