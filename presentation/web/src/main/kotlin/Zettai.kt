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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Zettai(val hub: ZettaiHub) : HttpHandler {
    val routes = routes(
        "/todo/{user}/{list}" bind GET to ::getToDoList,
        "/todo/{user}/{list}" bind POST to ::addNewItem,
        "/todo/{user}" bind GET to ::getAllLists,
        "/todo/{user}" bind POST to ::createNewList
    ).withFilter(ServerFilters.CatchAll { e ->
        when (e) {
            is IllegalStateException -> Response(NOT_FOUND)
            else -> Response(INTERNAL_SERVER_ERROR)
        }
    })

    private fun createNewList(request: Request): Response {
        val user = request.extractUser()
        val listName = request.extractListNameFromForm("listname")

        return listName
            ?.let { CreateToDoList(user, it) }
            ?.let(hub::handle)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}") }
            ?: Response(BAD_REQUEST)
    }

    private fun getAllLists(request: Request): Response {
        val user = request.extractUser()

        return hub.getLists(user)
            ?.let { renderListsPage(user, it) }
            ?.let(::createResponse)
            ?: Response(BAD_REQUEST)
    }

    private fun renderListsPage(user: User, lists: List<ListName>): HtmlPage =
        HtmlPage(
            """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
            <title>Zettai - a ToDoList application</title>
        </head>
        <body>
        <div id="container">
        <div class="row justify-content-md-center"> 
        <div class="col-md-center">
            <h1>Zettai</h1>
            <h2>User ${user.name}</h2>
            <table class="table table-hover">
                <thead>
                    <tr>
                      <th>Name</th>
                      <th>State</th>
                      <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                ${lists.render(user)}
                </tbody>
            </table>
            <hr>
            <h5>Create new to-do list</h5>
            <form action="/todo/${user.name}" method="post">
              <label for="listname">List name:</label>
              <input type="text" name="listname" id="listname">
              <input type="submit" value="Submit">
            </form>
            </div>
        </div>
        </div>
        </body>
        </html>
    """.trimIndent()
        )

    private fun List<ListName>.render(user: User): String =
        joinToString(separator = "") { renderListName(user, it) }


    private fun renderListName(user: User, listName: ListName): String = """<tr>
              <td><a href="${user.name}/${listName.name}">${listName.name}</a></td>
              <td>open</td>
              <td>[archive] [rename] [freeze]</td>
            </tr>""".trimIndent()

    private fun addNewItem(request: Request): Response {
        val user = request.path("user")?.let(::User) ?: return Response(BAD_REQUEST)
        val listName = request.path("list")?.let(ListName::fromUntrusted) ?: return Response(BAD_REQUEST)
        val item = request.form("itemname")?.let(::ToDoItem) ?: return Response(BAD_REQUEST)
        return hub.addItemToList(user, listName, item)
            ?.let { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            ?: Response(NOT_FOUND)
    }

    override fun invoke(request: Request): Response = routes(request)

    private fun getToDoList(request: Request): Response =
        (::extractListData andThen
                ::fetchListContent andThen
                ::renderPage andThen
                ::createResponse)(request)

    fun extractListData(request: Request): Pair<User, ListName> {
        val user = request.path("user").orEmpty()
        val list = request.path("list").orEmpty()
        return User(user) to ListName.fromTrusted(list)
    }

    fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        hub.getList(listId.first, listId.second)
            ?: error("List unknown")

    fun renderPage(todoList: ToDoList): HtmlPage =
        HtmlPage(
            """
        <!DOCTYPE html>
        <html>
        <head>
            <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">
            <title>Zettai - a ToDoList application</title>
        </head>
        <body>
        <div id="container">
        <div class="row justify-content-md-center"> 
        <div class="col-md-center">
            <h1>Zettai</h1>
            <h2>ToDo List ${todoList.listName.name}</h2>
            <table class="table table-hover">
                <thead>
                    <tr>
                      <th>Name</th>
                      <th>Due Date</th>
                      <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                ${todoList.renderItems()}
                </tbody>
            </table>
            </div>
        </div>
        </div>
        </body>
        </html>
    """.trimIndent()
        )

    private fun ToDoList.renderItems() =
        items.joinToString("", transform = ::renderItem)

    private fun renderItem(it: ToDoItem): String = """<tr>
              <td>${it.description}</td>
              <td>${it.dueDate?.toIsoString().orEmpty()}</td>
              <td>${it.status}</td>
            </tr>""".trimIndent()

    fun createResponse(html: HtmlPage): Response = Response(OK).body(html.raw)
}

private fun Request.extractListNameFromForm(formName: String): ListName? =
    form(formName)?.let(ListName::fromUntrusted)

private fun Request.extractUser(): User = path("user").orEmpty().let(::User)

private infix fun <A, B, C> ((A) -> B).andThen(next: (B) -> C): (A) -> C = { next(this(it)) }

data class HtmlPage(val raw: String)

fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun String?.toIsoLocalDate(): LocalDate? =
    unlessNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
    if (isNullOrEmpty()) null else f(this)

fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
