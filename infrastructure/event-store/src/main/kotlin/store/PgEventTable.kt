package store

import ZettaiOutcome
import ZettaiParsingError
import events.ItemAdded
import events.ItemModified
import events.ItemRemoved
import events.ListClosed
import events.ListCreated
import events.ListPutOnHold
import events.ListReleased
import events.ToDoListEvent
import fp.Outcome
import fp.OutcomeError
import fp.onFailure
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb

data class PgEventTable(override val tableName: String) : Table(tableName) {
    val id = long("id").autoIncrement()
    override val primaryKey = PrimaryKey(id, name = "${tableName}_pkey")

    val recorded_at = timestamp("recorded_at").defaultExpression(CurrentTimestamp)
    val entity_id = uuid("entity_id")
    val event_type = varchar("event_type", 100)
    val event_version = integer("event_version")
    val event_source = varchar("event_source", 100)
    val json_data = jsonb("json_data", { it }, { it })
}

data class Parser<A, S>(val render: (A) -> S, val parse: (S) -> Outcome<OutcomeError, A>) {
    fun parseOrThrow(encoded: S) =
        parse(encoded).onFailure { error("Error parsing $encoded ${it.msg}") }
}

data class PgEvent(
    val entityId: commands.EntityId,
    val eventType: String,
    val jsonString: String,
    val version: Int,
    val source: String
)

fun toDoListEventParser(): Parser<ToDoListEvent, PgEvent> = Parser(::toPgEvent, ::toToDoListEvent)

fun toPgEvent(event: ToDoListEvent): PgEvent =
    PgEvent(
        entityId = event.id,
        eventType = event::class.simpleName.orEmpty(),
        version = 1,
        source = "event store",
        jsonString = event.toJsonString()
    )

fun toToDoListEvent(pgEvent: PgEvent): ZettaiOutcome<ToDoListEvent> =
    Outcome.tryOrFail {
        when (pgEvent.eventType) {
            ListCreated::class.simpleName -> klaxon.parse<ListCreated>(pgEvent.jsonString)
            ItemAdded::class.simpleName -> klaxon.parse<ItemAdded>(pgEvent.jsonString)
            ItemRemoved::class.simpleName -> klaxon.parse<ItemRemoved>(pgEvent.jsonString)
            ItemModified::class.simpleName -> klaxon.parse<ItemModified>(pgEvent.jsonString)
            ListPutOnHold::class.simpleName -> klaxon.parse<ListPutOnHold>(pgEvent.jsonString)
            ListClosed::class.simpleName -> klaxon.parse<ListClosed>(pgEvent.jsonString)
            ListReleased::class.simpleName -> klaxon.parse<ListReleased>(pgEvent.jsonString)
            else -> null
        } ?: error("type not known ${pgEvent.eventType}")
    }.transformFailure { ZettaiParsingError("Error parsing ToDoListEvent: ${pgEvent} with error: $it ") }

fun ToDoListEvent.toJsonString() = when (this) {
    is ListCreated -> klaxon.toJsonString(this)
    is ItemAdded -> klaxon.toJsonString(this)
    is ItemRemoved -> klaxon.toJsonString(this)
    is ItemModified -> klaxon.toJsonString(this)
    is ListPutOnHold -> klaxon.toJsonString(this)
    is ListReleased -> klaxon.toJsonString(this)
    is ListClosed -> klaxon.toJsonString(this)
}
