package io.barinek.continuum.registration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.barinek.continuum.accounts.AccountController
import io.barinek.continuum.accounts.AccountDataGateway
import io.barinek.continuum.accounts.RegistrationController
import io.barinek.continuum.accounts.RegistrationService
import io.barinek.continuum.discovery.DiscoveryClient
import io.barinek.continuum.jdbcsupport.DataSourceConfig
import io.barinek.continuum.jdbcsupport.JdbcTemplate
import io.barinek.continuum.jdbcsupport.TransactionManager
import io.barinek.continuum.projects.ProjectControllerV1
import io.barinek.continuum.projects.ProjectControllerV2
import io.barinek.continuum.projects.ProjectDataGateway
import io.barinek.continuum.restsupport.BasicServer
import io.barinek.continuum.restsupport.DefaultController
import io.barinek.continuum.restsupport.RestTemplate
import io.barinek.continuum.users.UserController
import io.barinek.continuum.users.UserDataGateway
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
