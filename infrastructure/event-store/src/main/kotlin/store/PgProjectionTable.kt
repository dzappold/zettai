package zettai.store

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import zettai.fp.Outcome
import zettai.fp.ThrowableError
import zettai.projections.ToDoListProjectionRow

data class PgProjectionTable<ROW : Any>(override val tableName: String, val parser: Parser<ROW, String>) : Table(tableName) {
    val id = varchar("id", 50)
    override val primaryKey = PrimaryKey(id, name = "${tableName}_pkey")

    val updated_at = timestamp("updated_at").defaultExpression(CurrentTimestamp)
    val row_data = jsonb("row_data", parser::render.get(), parser::parseOrThrow)
}

data class PgLastEventTable(override val tableName: String) : Table(tableName) {
    val last_event_id = long("last_event_id")
    val recorded_at = timestamp("recorded_at").defaultExpression(CurrentTimestamp)
}

val toDoListProjectionParser = Parser(
    parse = ::readProjectionRow,
    render = ::writeProjectionRow
)

fun writeProjectionRow(row: ToDoListProjectionRow): String = klaxon.toJsonString(row)

fun readProjectionRow(json: String): Outcome<ThrowableError, ToDoListProjectionRow> =
    Outcome.tryOrFail { klaxon.parse(json) ?: error("Empty row $json") }
