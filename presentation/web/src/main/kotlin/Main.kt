import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {

    data class ToDoListFetcherFromMap(private val store: ToDoListStore) : ToDoListFetcher, ToDoListUpdatableFetcher {
        override fun invoke(user: User, listName: ListName): ToDoList? =
            store[user]?.get(listName)

        override fun assignListToUser(user: User, list: ToDoList): ToDoList? =
            store.compute(user) { _, value ->
                val listMap = value ?: mutableMapOf()
                listMap.apply { put(list.listName, list) }
            }?.let { list }
    }


    val items = listOf("write chapter", "insert code", "draw diagrams")
    val toDoList = ToDoList(ListName("book"), items.map(::ToDoItem))
    fun initialStore(): ToDoListStore = mutableMapOf(User("uberto") to mutableMapOf(toDoList.listName to toDoList))
    val fetcher = ToDoListFetcherFromMap(initialStore())
    Zettai(ToDoListHub(fetcher)).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto/book")
}

typealias ToDoListStore = MutableMap<User, MutableMap<ListName, ToDoList>>
