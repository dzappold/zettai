import commands.ToDoListCommandHandler
import events.ToDoListEventStore
import events.ToDoListEventStreamerInMemory
import org.http4k.server.Jetty
import org.http4k.server.asServer
import projections.ToDoListQueryRunner

fun main() {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val commandHandler = ToDoListCommandHandler(eventStore)
    val queryHandler = ToDoListQueryRunner(streamer::fetchAfter)

    val hub = ToDoListHub(queryHandler, commandHandler, eventStore)

    Zettai(hub)
        .asServer(Jetty(8080))
        .start()
        .also { server ->
            Runtime
                .getRuntime()
                .addShutdownHook(Thread { server.stop() })
        }

    println("Server started at http://localhost:8080/todo/uberto")
}
