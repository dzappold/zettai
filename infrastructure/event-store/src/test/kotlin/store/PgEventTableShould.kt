package store

import User
import commands.ToDoListId
import events.EventSeq
import events.ListCreated
import events.StoredEvent
import events.ToDoListEvent
import io.kotest.matchers.ints.shouldBeGreaterThan
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import randomToDoList
import javax.sql.DataSource

class PgEventTableShould {
    val dataSource: DataSource = pgDataSourceForTest()
    val list = randomToDoList()
    val user = User("uberto")

    @BeforeEach
    fun initializeDatabase() {
        val db = Database.connect(dataSource)
        transaction(db) {
            SchemaUtils.create(toDoListEventsTable)
        }
    }

    @Test
    fun `can read and write events from db`() {
        val db = Database.connect(dataSource)

        transaction(db) {
            val listId = ToDoListId.mint()
            val event = ListCreated(listId, user, list.listName)
            val pgEvent = toPgEvent(event)

            val eventId = toDoListEventsTable.insertIntoWithReturn(this, stored(event)) { newRow ->
                newRow[entity_id] = pgEvent.entityId.raw
                newRow[event_source] = pgEvent.source
                newRow[event_type] = pgEvent.eventType
                newRow[json_data] = pgEvent.jsonString
                newRow[event_version] = pgEvent.version

            }.eventSeq

            eventId.progressive.shouldBeGreaterThan(0)
            val row = toDoListEventsTable.selectWhere(this, toDoListEventsTable.id.eq(eventId.progressive.toLong())).single()
        }
    }
}

fun pgDataSourceForTest(): PgDataSource =
    PgDataSource.create(
        host = "localhost",
        port = 6433,
        database = "zettai_db",
        dbUser = "zettai_admin",
        dbPassword = "zettai!"
    )

class PgDataSource private constructor(
    val name: String,
    private val delegate: PGSimpleDataSource
) : DataSource by delegate {

    companion object {
        fun create(
            host: String,
            port: Int,
            dbUser: String,
            dbPassword: String,
            database: String
        ): PgDataSource = PGSimpleDataSource().apply {
            serverNames = arrayOf(host)
            portNumbers = intArrayOf(port)
            databaseName = database
            user = dbUser
            password = dbPassword
        }.let {
            PgDataSource("$host:$port:$database", it)
        }
    }
}

private fun stored(event: ToDoListEvent): InsertStatement<Number>.() -> StoredEvent<ToDoListEvent> =
    {
        StoredEvent(EventSeq(get(toDoListEventsTable.id).toInt()), event)
    }
