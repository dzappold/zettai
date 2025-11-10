package zettai

import org.junit.jupiter.api.Assertions.fail
import zettai.commands.ToDoListCommandHandler
import zettai.events.ToDoListEventStore
import zettai.events.ToDoListEventStreamerInMemory
import zettai.fp.Outcome
import zettai.fp.OutcomeError
import zettai.fp.onFailure
import zettai.projections.ToDoListQueryRunner

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
