import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.body.form
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai(val hub: ZettaiHub) : HttpHandler {
    val routes = routes(
        "/todo/{user}/{list}" bind GET to ::getToDoList,
        "/todo/{user}/{list}" bind POST to ::addNewItem,
    ).withFilter(ServerFilters.CatchAll { e ->
        when (e) {
            is IllegalStateException -> Response(NOT_FOUND)
            else -> Response(INTERNAL_SERVER_ERROR)
        }
    })

    private fun addNewItem(request: Request): Response {
        val user = request.path("user")?.let(::User) ?: return Response(BAD_REQUEST)
        val listName = request.path("list")?.let(::ListName) ?: return Response(BAD_REQUEST)
        val item = request.form("itemname")?.let(::ToDoItem) ?: return Response(BAD_REQUEST)
        return hub.addItemToList(user, listName, item)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(NOT_FOUND)
    }

    override fun invoke(request: Request): Response = routes(request)

    private fun getToDoList(request: Request): Response =
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

data class HtmlPage(val raw: String)
