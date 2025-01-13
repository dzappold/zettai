package DDT.actions

import DDT.actors.ToDoListOwner
import DDT.actors.expectSuccess
import ListName
import ToDoItem
import ToDoList
import ToDoListHub
import User
import ZettaiOutcome
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready
import commands.AddToDoItem
import commands.CreateToDoList
import commands.ToDoListCommandHandler
import events.ToDoListEventStore
import events.ToDoListEventStreamerInMemory
import projections.ToDoListQueryRunner
import strikt.api.expectThat
import strikt.assertions.hasSize

class DomainOnlyActions : ZettaiActions {
    override val protocol = DomainOnly
    override fun prepare() = Ready

    private val eventStreamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(eventStreamer)
    private val commandHandler = ToDoListCommandHandler(eventStore)
    private val queryRunner = ToDoListQueryRunner(eventStreamer::fetchAfter)

    private val hub by lazy { ToDoListHub(queryRunner, commandHandler, eventStore) }

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val list = ListName.fromTrusted(listName)

        hub.handle(CreateToDoList(user, list)).expectSuccess()

        val events = items.map { description ->
            hub.handle(AddToDoItem(user, list, ToDoItem(description))).expectSuccess()
        }
        expectThat(events).hasSize(items.size)
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> =
        hub.getLists(user)

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        hub.getList(user, listName)

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.handle(AddToDoItem(user, listName, item))
    }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> =
        hub.whatsNext(user)
}
