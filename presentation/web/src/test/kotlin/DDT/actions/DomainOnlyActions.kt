package DDT.actions

import AddToDoItem
import CreateToDoList
import DDT.actors.ToDoListOwner
import DDT.actors.expectSuccess
import InvalidRequestError
import ListName
import ToDoItem
import ToDoList
import ToDoListCommandHandler
import ToDoListEventStore
import ToDoListEventStreamerInMemory
import ToDoListFetcherFromMap
import ToDoListHub
import ToDoListStore
import User
import ZettaiOutcome
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import failIfNull
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyActions : ZettaiActions {
    override val protocol = DomainOnly
    override fun prepare() = Ready

    private fun emptyStore(): ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(emptyStore())

    private val eventStreamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(eventStreamer)
    private val commandHandler = ToDoListCommandHandler(eventStore, fetcher)

    private val hub by lazy { ToDoListHub(fetcher, commandHandler, eventStore) }

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)

        hub.handle(CreateToDoList(user, list)).expectSuccess()

        val created = items.map { description ->
            hub.handle(AddToDoItem(user, list, ToDoItem(description)))
        }
        expectThat(created).hasSize(items.size)
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> =
        fetcher
            .getAll(user)
            .failIfNull(InvalidRequestError("something"))

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> {
        return hub.getList(user, listName)
    }

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }
}
