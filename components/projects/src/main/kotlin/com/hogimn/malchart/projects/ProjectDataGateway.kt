package com.hogimn.malchart.projects

import com.hogimn.malchart.jdbcsupport.JdbcTemplate

class ProjectDataGateway(val jdbcTemplate: JdbcTemplate) {

    fun create(accountId: Long, name: String, active: Boolean = true, funded: Boolean = false): ProjectRecord {
        val sql = "insert into projects (account_id, name, active, funded) values (?, ?, ?, ?)"
        return jdbcTemplate.createWithGeneratedKeys(sql, { id -> ProjectRecord(id, accountId, name, active, funded) }, accountId, name, active, funded)
    }

    fun findBy(accountId: Long): List<ProjectRecord> {
        val sql = "select id, account_id, name, active, funded from projects where account_id = ? order by name asc"
        return jdbcTemplate.findBy(sql, { rs -> ProjectRecord(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBoolean(4), rs.getBoolean(5)) }, accountId)
    }

    fun findObject(projectId: Long): ProjectRecord? {
        val sql = "select id, account_id, name, active, funded from projects where id = ? order by name asc"
        return jdbcTemplate.findObject(sql, { rs -> ProjectRecord(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getBoolean(4), rs.getBoolean(5)) }, projectId)
    }
}
