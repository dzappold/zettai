typealias ToDoListStore = MutableMap<User, MutableMap<ListName, ToDoList>>

data class ToDoListFetcherFromMap(private val store: ToDoListStore) : ToDoListFetcher, ToDoListUpdatableFetcher {
    override fun get(user: User, listName: ListName): ToDoList? =
        store[user]?.get(listName)

    override fun assignListToUser(user: User, list: ToDoList): ToDoList? =
        store.compute(user) { _, value ->
            val listMap = value ?: mutableMapOf()
            listMap.apply { put(list.listName, list) }
        }?.let { list }

    override fun getAll(user: User): List<ListName>? =
        store[user]
            ?.map { (listName, _) -> listName }
            ?: emptyList()
}
