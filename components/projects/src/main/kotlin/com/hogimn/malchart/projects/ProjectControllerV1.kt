package com.hogimn.malchart.projects

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController

class ProjectControllerV1(val mapper: ObjectMapper, val gateway: ProjectDataGateway) : BasicController() {
    private val mediaTypes = listOf("application/json", "application/vnd.appcontinuum.v1+json")

    override fun handle(exchange: HttpExchange): Boolean {
        return post(exchange, "/projects", mediaTypes) {
            val project = mapper.readValue(body(exchange), ProjectInfoV1::class.java)
            val record = gateway.create(project.accountId, project.name)
            mapper.writeValueAsString(ProjectInfoV1(record.id, record.accountId, record.name, record.active, "project info"))
        } || get(exchange, "/projects", mediaTypes) {
            val accountId = parameters(exchange)["accountId"]!!
            val list = gateway.findBy(accountId.toLong()).map { record ->
                ProjectInfoV1(record.id, record.accountId, record.name, record.active, "project info")
            }
            mapper.writeValueAsString(list)
        } || get(exchange, "/project", mediaTypes) {
            val projectId = parameters(exchange)["projectId"]!!
            val record = gateway.findObject(projectId.toLong())
            if (record != null) {
                mapper.writeValueAsString(ProjectInfoV1(record.id, record.accountId, record.name, record.active, "project info"))
            } else {
                throw IllegalStateException("Project with id $projectId not found")
            }
        }
    }
}
