package store

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

val toDoListEventsTable = PgEventTable("todo_list_events")

val toDoListProjectionTable = PgProjectionTable("todo_list_projection", toDoListProjectionParser)

val toDoListLastEventTable = PgLastEventTable("${toDoListProjectionTable.tableName}_last_processed_event")

fun resetDatabase(datasource: DataSource) {
    val db = Database.connect(datasource)

    transaction(db) {
        addLogger(StdOutSqlLogger)
        dropTables()
        prepareDb()
    }
}

fun prepareDatabase(datasource: DataSource) {
    val db = Database.connect(datasource)

    transaction(db) {
        addLogger(StdOutSqlLogger)

        prepareDb()
    }
}

private fun Transaction.prepareDb() {
    SchemaUtils.create(
        toDoListEventsTable,
        toDoListProjectionTable,
        toDoListLastEventTable
    )
}

private fun dropTables() {
    SchemaUtils.drop(
        toDoListEventsTable,
        toDoListProjectionTable,
        toDoListLastEventTable
    )
}
