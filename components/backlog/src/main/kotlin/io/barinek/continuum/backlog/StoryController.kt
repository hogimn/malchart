package io.barinek.continuum.backlog

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import io.barinek.continuum.restsupport.BasicController

class StoryController(val mapper: ObjectMapper, val gateway: StoryDataGateway, val client: ProjectClient) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return post(exchange, "/stories", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val story = mapper.readValue(body(exchange), StoryInfo::class.java)

            if (projectIsActive(story.projectId)) {
                val record = gateway.create(story.projectId, story.name)
                mapper.writeValueAsString(StoryInfo(record.id, record.projectId, record.name, "story info"))
            } else {
                throw IllegalStateException("Project ${story.projectId} is not active")
            }
        } || get(exchange, "/stories", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val projectId = parameters(exchange)["projectId"]!!
            val list = gateway.findBy(projectId.toLong()).map { record ->
                StoryInfo(record.id, record.projectId, record.name, "story info")
            }
            mapper.writeValueAsString(list)
        }
    }

    private fun projectIsActive(projectId: Long): Boolean {
        val project = client.getProject(projectId)
        return project != null && project.active
    }
}
