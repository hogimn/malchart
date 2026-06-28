package com.hogimn.malchart.users

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController

class UserController(val mapper: ObjectMapper, val gateway: UserDataGateway) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return get(exchange, "/users", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val userId = parameters(exchange)["userId"]!!
            val record = gateway.findObjectBy(userId.toLong())
            mapper.writeValueAsString(UserInfo(record.id, record.name, "user info"))
        }
    }
}
