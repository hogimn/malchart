package com.hogimn.malchart.registration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.hogimn.malchart.accounts.AccountController
import com.hogimn.malchart.accounts.AccountDataGateway
import com.hogimn.malchart.accounts.RegistrationController
import com.hogimn.malchart.accounts.RegistrationService
import com.hogimn.malchart.discovery.DiscoveryClient
import com.hogimn.malchart.jdbcsupport.DataSourceConfig
import com.hogimn.malchart.jdbcsupport.JdbcTemplate
import com.hogimn.malchart.jdbcsupport.TransactionManager
import com.hogimn.malchart.projects.ProjectControllerV1
import com.hogimn.malchart.projects.ProjectControllerV2
import com.hogimn.malchart.projects.ProjectDataGateway
import com.hogimn.malchart.restsupport.BasicServer
import com.hogimn.malchart.restsupport.DefaultController
import com.hogimn.malchart.restsupport.RestTemplate
import com.hogimn.malchart.users.UserController
import com.hogimn.malchart.users.UserDataGateway
import java.lang.System.getenv
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class App(val url: String, port: Int) : BasicServer(port) {
    val mapper: ObjectMapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    override fun registerContexts() {
        val dataSource = DataSourceConfig().createDataSource(url)
        val transactionManager = TransactionManager(dataSource)
        val template = JdbcTemplate(dataSource)

        val userDataGateway = UserDataGateway(template)
        val accountDataGateway = AccountDataGateway(template)
        val projectDataGateway = ProjectDataGateway(template)

        val projectControllerV1 = ProjectControllerV1(mapper, projectDataGateway)
        val projectControllerV2 = ProjectControllerV2(mapper, projectDataGateway)

        context("/registration", RegistrationController(mapper, RegistrationService(transactionManager, userDataGateway, accountDataGateway)))
        context("/accounts", AccountController(mapper, accountDataGateway))
        context("/users", UserController(mapper, userDataGateway))
        context("/projects", projectControllerV1, projectControllerV2)
        context("/project", projectControllerV1, projectControllerV2)
        context("/", DefaultController())
    }

    override fun start() {
        super.start()
        Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory()).scheduleAtFixedRate({
            DiscoveryClient(mapper, RestTemplate()).heartbeat("registration", uri())
        }, 0L, 30L, TimeUnit.SECONDS)
    }
}

fun main() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    val url = getenv("DATABASE_URL")
    val port = getenv("PORT").toInt()
    App(url, port).start()
}
