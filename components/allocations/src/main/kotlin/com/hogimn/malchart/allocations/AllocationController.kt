package com.hogimn.malchart.allocations

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController

class AllocationController(val mapper: ObjectMapper, val gateway: AllocationDataGateway, val client: ProjectClient) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return post(exchange, "/allocations", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val allocation = mapper.readValue(body(exchange), AllocationInfo::class.java)

            if (projectIsActive(allocation.projectId)) {
                val record = gateway.create(allocation.projectId, allocation.userId, allocation.firstDay, allocation.lastDay)
                mapper.writeValueAsString(AllocationInfo(record.id, record.projectId, record.userId, record.firstDay, record.lastDay, "allocation info"))
            } else {
                throw IllegalStateException("Project ${allocation.projectId} is not active")
            }
        } || get(exchange, "/allocations", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val projectId = parameters(exchange)["projectId"]!!
            val list = gateway.findBy(projectId.toLong()).map { record ->
                AllocationInfo(record.id, record.projectId, record.userId, record.firstDay, record.lastDay, "allocation info")
            }
            mapper.writeValueAsString(list)
        }
    }

    private fun projectIsActive(projectId: Long): Boolean {
        val project = client.getProject(projectId)
        return project != null && project.active
    }
}
