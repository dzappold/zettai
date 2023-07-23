import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes

class Zettai : HttpHandler {
    val routes = routes(
        "/todo/{user}/{list}" bind GET to ::showList,
    )

    override fun invoke(request: Request): Response = routes(request)

    private fun showList(request: Request): Response {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        val htmlPage = """
    <html>
        <body>
            <h1>Zettai</h1>
            <p>Here is the list <b>$list</b> of user <b>$user</b></p>
        </body>
    </html>
    """.trimIndent()
        return Response(OK).body(htmlPage)
    }

}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)
data class ListName(val name: String)
data class User(val name: String)
data class ToDoItem(val description: String)
enum class ToDoStatus { Todo, InProgress, Done, Blocked }
