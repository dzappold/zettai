class ToDoListHub(
    private val fetcher: ToDoListUpdatableFetcher,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher.get(user, listName)

    override fun getLists(user: User): List<ListName>? =
        fetcher.getAll(user)

    override fun handle(command: ToDoListCommand): ToDoListCommand? =
        commandHandler(command)
            ?.let(persistEvents)
            ?.let { command }
}

interface ToDoListFetcher {
    fun get(user: User, listName: ListName): ToDoList?
    fun getAll(user: User): List<ListName>?
}

typealias ToDoListMap = Map<User, Map<ListName, ToDoList>>

fun mapFetcher(map: ToDoListMap, user: User, listName: ListName): ToDoList? =
    map[user]?.get(listName)

fun <A, B, C, R> partial(f: (A, B, C) -> R, a: A): (B, C) -> R =
    { b, c -> f(a, b, c) }

//val fetcher: ToDoListFetcher = partial(::mapFetcher, map)
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
