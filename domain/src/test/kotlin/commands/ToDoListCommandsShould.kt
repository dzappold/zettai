package commands

import InconsistentStateError
import ToDoListCommandError
import events.ListCreated
import events.ToDoListEventStore
import events.ToDoListEventStreamerInMemory
import expectFailure
import expectSuccess
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
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
