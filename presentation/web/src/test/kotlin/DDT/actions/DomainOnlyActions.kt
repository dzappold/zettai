package DDT.actions

import AddToDoItem
import CreateToDoList
import ListName
import ToDoItem
import ToDoList
import ToDoListCommandHandler
import ToDoListEventStore
import ToDoListEventStreamerInMemory
import ToDoListFetcherFromMap
import ToDoListHub
import DDT.actors.ToDoListOwner
import ToDoListStore
import User
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
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
        val name = ListName.fromTrusted(listName)

        val command = hub.handle(CreateToDoList(user, name))
        command ?: error("failed to create list $listName")

        val created = items.mapNotNull { description ->
            hub.handle(AddToDoItem(user, name, ToDoItem(description)))
        }
        expectThat(created).hasSize(items.size)
    }

    override fun allUserLists(user: User): List<ListName> {
        return fetcher.getAll(user) ?: emptyList()
    }

    override fun createList(user: User, listName: ListName) {
        hub.handle(CreateToDoList(user, listName))
    }

    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName)

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }
}
