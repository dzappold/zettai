class ToDoListHub(private val fetcher: ToDoListUpdatableFetcher) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher.get(user, listName)

    override fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList? =
        fetcher.get(user, listName)?.run {
            val newList = copy(items = replaceItem(item))
            fetcher.assignListToUser(user, newList)
        }

    override fun getLists(user: User): List<ListName>? =
        fetcher.getAll(user)

    override fun createToDoList(user: User, listName: ListName) {
        TODO("Not yet implemented")
    }
}

fun ToDoList.replaceItem(item: ToDoItem) =
    items.filterNot { it.description == item.description } + item

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
}
