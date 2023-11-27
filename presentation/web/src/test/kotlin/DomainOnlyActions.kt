import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.Ready

class DomainOnlyActions : ZettaiActions {
    override val protocol = DomainOnly
    override fun prepare() = Ready

    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
    private val hub by lazy { ToDoListHub(lists) }
    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        lists += (this.user to listOf(ToDoList(ListName(listName), items.map(::ToDoItem))))
    }

    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName)
}
