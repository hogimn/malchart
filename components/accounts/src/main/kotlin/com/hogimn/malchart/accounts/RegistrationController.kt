package com.hogimn.malchart.accounts

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController
import com.hogimn.malchart.users.UserInfo

class RegistrationController(val mapper: ObjectMapper, val service: RegistrationService) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return post(exchange, "/registration", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val user = mapper.readValue(body(exchange), UserInfo::class.java)
            val record = service.createUserWithAccount(user.name)
            mapper.writeValueAsString(UserInfo(record.id, record.name, "registration info"))
        }
    }
}
