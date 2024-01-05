import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready

class DomainOnlyActions : ZettaiActions {
    override val protocol = DomainOnly
    override fun prepare() = Ready

    fun emptyStore(): ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(emptyStore())

    private val hub by lazy { ToDoListHub(fetcher) }
    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        fetcher.assignListToUser(user, ToDoList(ListName.fromTrusted(listName), items.map(::ToDoItem)))
    }

    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName)

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        hub.addItemToList(user, listName, item)
    }
}
