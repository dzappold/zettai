import commands.ToDoListCommandHandler
import events.ToDoListEventStore
import events.ToDoListEventStreamerInMemory
import projections.ToDoListQueryRunner

fun prepareToDoListHubForTests(): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    return ToDoListHub(queryRunner, cmdHandler, eventStore)
}
