import commands.ToDoListCommandHandler
import commands.expectFailure
import commands.expectSuccess
import events.ToDoListEventStore
import events.ToDoListEventStreamer
import events.ToDoListEventStreamerInMemory
import fp.Success
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ToDoListHubShould {
    private fun emptyStore(): ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(emptyStore())

    private val eventStreamer: ToDoListEventStreamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(eventStreamer)
    private val commandHandler: ToDoListCommandHandler = ToDoListCommandHandler(eventStore, fetcher)

    private val hub = ToDoListHub(fetcher, commandHandler, eventStore)

    @Test
    fun `get list by user and name`() {
        repeat(10) {
            val user = randomUser()
            val list = randomToDoList()

            fetcher.assignListToUser(user, list)

            val myList = hub.getList(user, list.listName)
            myList.expectSuccess()
            (myList as Success).value shouldBe list
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstUser = randomUser()
            val firstList = randomToDoList()

            val secondUser = randomUser()
            val secondList = randomToDoList()

            fetcher.assignListToUser(firstUser, firstList)
            fetcher.assignListToUser(secondUser, secondList)

            hub.getList(firstUser, secondList.listName).expectFailure()
            hub.getList(secondUser, firstList.listName).expectFailure()
        }
    }

}
