package io.barinek.continuum.timesheets

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import io.barinek.continuum.restsupport.BasicController

class TimeEntryController(val mapper: ObjectMapper, val gateway: TimeEntryDataGateway, val client: ProjectClient) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return post(exchange, "/time-entries", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val entry = mapper.readValue(body(exchange), TimeEntryInfo::class.java)

            if (projectIsFunded(entry.projectId)) {
                val record = gateway.create(entry.projectId, entry.userId, entry.date, entry.hours)
                mapper.writeValueAsString(TimeEntryInfo(record.id, record.projectId, record.userId, record.date, record.hours, "entry info"))
            } else {
                throw IllegalStateException("Project ${entry.projectId} is not active")
            }
        } || get(exchange, "/time-entries", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val userId = parameters(exchange)["userId"]!!
            val list = gateway.findBy(userId.toLong()).map { record ->
                TimeEntryInfo(record.id, record.projectId, record.userId, record.date, record.hours, "entry info")
            }
            mapper.writeValueAsString(list)
        }
    }

    private fun projectIsFunded(projectId: Long): Boolean {
        val project = client.getProject(projectId)
        return project != null && (project.active && project.funded)
    }
}