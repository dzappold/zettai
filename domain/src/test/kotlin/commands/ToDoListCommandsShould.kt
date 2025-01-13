package commands

import InconsistentStateError
import ToDoListCommandError
import events.ListCreated
import events.ToDoListEventStore
import events.ToDoListEventStreamerInMemory
import fp.Outcome
import fp.OutcomeError
import fp.onFailure
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import randomItem
import randomListName
import randomUser

class ToDoListCommandsShould {
    private val streamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(streamer)

    private val handler = ToDoListCommandHandler(eventStore)

    private val name = randomListName()
    private val user = randomUser()

    @Test
    fun `Add list fails if the user has already a list with same name`() {
        val cmd = CreateToDoList(user, name)
        val result = handler(cmd).expectSuccess()

        result.shouldHaveSize(1)
        result.single().shouldBeInstanceOf<ListCreated>()
        eventStore(result)

        val duplicatedResult = handler(cmd).expectFailure()
        duplicatedResult.shouldBeInstanceOf<InconsistentStateError>()
    }

    @Test
    fun `Add items fails if the list doesn't exists`() {
        val cmd = AddToDoItem(user, name, randomItem())
        val result = handler(cmd).expectFailure()
        result.shouldBeInstanceOf<ToDoListCommandError>()
    }


}

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T =
    onFailure { error -> fail { "$this expected success but was $error" } }


fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }
