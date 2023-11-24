import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai(val hub: ZettaiHub) : HttpHandler {
    val routes = routes(
        "/todo/{user}/{list}" bind GET to ::showList,
    )

    override fun invoke(request: Request): Response = routes(request)

    private fun showList(request: Request): Response =
        (::extractListData andThen
                ::fetchListContent andThen
                ::renderHtml andThen
                ::createResponse)(request)

    fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName(list)
    }

    fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        hub.getList(listId.first, listId.second)
            ?: error("List unknown")

    fun renderHtml(todoList: ToDoList): HtmlPage = HtmlPage(
        """
    <html>
        <body>
            <h1>Zettai</h1>
            <h2>${todoList.listName.name}</h2>
            <table>
            <tbody>${renderItems(todoList.items)}</tbody>
            </table>
        </body>
    </html>
    """.trimIndent()
    )

    fun renderItems(items: List<ToDoItem>) =
        items.joinToString("") { """<tr><td>${it.description}</td></tr>""" }

    fun createResponse(html: HtmlPage): Response = Response(OK).body(html.raw)
}

private infix fun <A, B, C> ((A) -> B).andThen(next: (B) -> C): (A) -> C = { next(this(it)) }

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)
data class ListName(val name: String)
data class User(val name: String)
data class ToDoItem(val description: String)
enum class ToDoStatus { Todo, InProgress, Done, Blocked }
data class HtmlPage(val raw: String)

