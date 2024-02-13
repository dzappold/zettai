import commands.ToDoListCommand
import commands.ToDoListCommandHandler
import events.EventPersister
import events.ToDoListEvent
import fp.failIfNull

class ToDoListHub(
    private val fetcher: ToDoListUpdatableFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {
    override fun getList(user: User, listName: ListName) =
        fetcher
            .get(user, listName)
            .failIfNull(InvalidRequestError("List $listName of user $user not found!"))

    override fun getLists(user: User) =
        fetcher
            .getAll(user)
            .failIfNull(InvalidRequestError("Lists for user $user not found!"))

    override fun handle(command: ToDoListCommand) =
        commandHandler(command)
            .transform(persistEvents)
            .transform { command }
}

interface ToDoListFetcher {
    fun get(user: User, listName: ListName): ToDoList?
    fun getAll(user: User): List<ListName>?
}

interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(user: User, list: ToDoList): ToDoList?
    fun addItemToList(user: User, name: ListName, item: ToDoItem): ToDoList? =
        get(user, name)?.run {
            val newList = copy(items = replaceItem(item))
            assignListToUser(user, newList)
        }
}

fun ToDoList.replaceItem(item: ToDoItem) =
    items.filterNot { it.description == item.description } + item
