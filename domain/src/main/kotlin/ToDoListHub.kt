class ToDoListHub(private val fetcher: ToDoListUpdatableFetcher) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ToDoList? =
        fetcher(user, listName)

    override fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList? =
        fetcher(user, listName)?.run {
            val newList = copy(items = items.filterNot { it.description == item.description } + item)
            fetcher.assignListToUser(user, newList)
        }
}

typealias ToDoListFetcher = (User, ListName) -> ToDoList?
typealias ToDoListMap = Map<User, Map<ListName, ToDoList>>

fun mapFetcher(map: ToDoListMap, user: User, listName: ListName): ToDoList? =
    map[user]?.get(listName)

fun <A, B, C, R> partial(f: (A, B, C) -> R, a: A): (B, C) -> R =
    { b, c -> f(a, b, c) }

//val fetcher: ToDoListFetcher = partial(::mapFetcher, map)
interface ToDoListUpdatableFetcher : ToDoListFetcher {
    fun assignListToUser(user: User, list: ToDoList): ToDoList?
}
