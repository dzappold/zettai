import org.http4k.server.Jetty
import org.http4k.server.asServer

fun main() {
    val items = listOf("write chapter", "insert code", "draw diagrams")
    val toDoList = ToDoList(ListName("book"), items.map(::ToDoItem))
    fun initialStore(): ToDoListStore = mutableMapOf(User("uberto") to mutableMapOf(toDoList.listName to toDoList))
    val fetcher = ToDoListFetcherFromMap(initialStore())
    Zettai(ToDoListHub(fetcher)).asServer(Jetty(8080)).start()

    println("Server started at http://localhost:8080/todo/uberto/book")
}
