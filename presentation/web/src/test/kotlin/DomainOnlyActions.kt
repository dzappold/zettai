import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready

class DomainOnlyActions : ZettaiActions {
    override val protocol = DomainOnly
    override fun prepare() = Ready

    fun emptyStore(): ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(emptyStore())

    private val eventStreamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(eventStreamer)
    private val commandHandler = ToDoListCommandHandler(eventStore, fetcher)

    private val hub by lazy { ToDoListHub(fetcher, commandHandler, eventStore) }
    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        fetcher.assignListToUser(user, ToDoList(ListName.fromTrusted(listName), items.map(::ToDoItem)))
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
        hub.addItemToList(user, listName, item)
    }
}
