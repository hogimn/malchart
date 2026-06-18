package io.barinek.continuum.projects

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import io.barinek.continuum.restsupport.BasicController

class ProjectControllerV2(val mapper: ObjectMapper, val gateway: ProjectDataGateway) : BasicController() {
    private val mediaTypes = listOf("application/vnd.appcontinuum.v2+json")

    override fun handle(exchange: HttpExchange): Boolean {
        return post(exchange, "/projects", mediaTypes) {
            val project = mapper.readValue(body(exchange), ProjectInfoV2::class.java)
            val record = gateway.create(project.accountId, project.name, project.active, project.funded)
            mapper.writeValueAsString(ProjectInfoV2(record.id, record.accountId, record.name, record.active, record.funded, "project info"))
        } || get(exchange, "/projects", mediaTypes) {
            val accountId = parameters(exchange)["accountId"]!!
            val list = gateway.findBy(accountId.toLong()).map { record ->
                ProjectInfoV2(record.id, record.accountId, record.name, record.active, record.funded, "project info")
            }
            mapper.writeValueAsString(list)
        } || get(exchange, "/project", mediaTypes) {
            val projectId = parameters(exchange)["projectId"]!!
            val record = gateway.findObject(projectId.toLong())
            if (record != null) {
                mapper.writeValueAsString(ProjectInfoV2(record.id, record.accountId, record.name, record.active, record.funded, "project info"))
            } else {
                throw IllegalStateException("Project with id $projectId not found")
            }
        }
    }
}
