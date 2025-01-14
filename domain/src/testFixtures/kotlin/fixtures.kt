import commands.ToDoListCommandHandler
import events.ToDoListEventStore
import events.ToDoListEventStreamerInMemory
import fp.Outcome
import fp.OutcomeError
import fp.onFailure
import org.junit.jupiter.api.Assertions.fail
import projections.ToDoListQueryRunner

fun prepareToDoListHubForTests(): ToDoListHub {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)
    val cmdHandler = ToDoListCommandHandler(eventStore)
    val queryRunner = ToDoListQueryRunner(streamer::fetchAfter)
    return ToDoListHub(queryRunner, cmdHandler, eventStore)
}

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T =
    onFailure { error -> fail { "$this expected success but was $error" } }


fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }
