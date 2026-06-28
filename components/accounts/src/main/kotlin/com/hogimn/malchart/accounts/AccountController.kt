package com.hogimn.malchart.accounts

import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpExchange
import com.hogimn.malchart.restsupport.BasicController

class AccountController(val mapper: ObjectMapper, val gateway: AccountDataGateway) : BasicController() {

    override fun handle(exchange: HttpExchange): Boolean {
        return get(exchange, "/accounts", listOf("application/json", "application/vnd.appcontinuum.v1+json")) {
            val ownerId = parameters(exchange)["ownerId"]!!
            val list = gateway.findBy(ownerId.toLong()).map { record ->
                AccountInfo(record.id, record.ownerId, record.name, "account info")
            }
            mapper.writeValueAsString(list)
        }
    }
}
