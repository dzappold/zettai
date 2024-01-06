import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.Uri
import org.http4k.core.body.form
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.filter.ServerFilters
import org.http4k.lens.Header
import org.http4k.lens.Path
import org.http4k.lens.nonBlankString
import org.http4k.routing.bind
import org.http4k.routing.routes
import ui.HtmlPage
import ui.renderListsPage
import ui.renderPage

val userLens = Path.nonBlankString().map(::User, User::name).of("user")
val listNameLens =
    Path.nonBlankString().map({ ListName.fromUntrusted(it) ?: error("invalid list name") }, ListName::name).of("list")

class Zettai(private val hub: ZettaiHub) : HttpHandler {
    private val routes = routes(
        "/todo/{user}/{list}" bind GET to ::getToDoList,
        "/todo/{user}/{list}" bind POST to ::addNewItem,
        "/todo/{user}" bind GET to ::getAllLists,
        "/todo/{user}" bind POST to ::createNewList
    ).withFilter(ServerFilters.CatchAll().then(ServerFilters.CatchLensFailure()))

    override fun invoke(request: Request): Response = routes(request)

    private fun getToDoList(request: Request): Response {
        val user = userLens(request)
        val listName = listNameLens(request)

        return hub.getList(user, listName)
            ?.let { toDoList -> renderPage(toDoList) }
            ?.let { renderPage -> createResponse(renderPage) }
            ?: Response(NOT_FOUND)
    }

    private fun addNewItem(request: Request): Response {
        val user = userLens(request)
        val listName = listNameLens(request)
        val item = request.form("itemname")?.let(::ToDoItem) ?: return Response(BAD_REQUEST)

        return AddToDoItem(user, listName, item)
            .let(hub::handle)
            ?.let { Response(SEE_OTHER).with(Header.LOCATION of Uri.of("/todo/${user.name}/${listName.name}")) }
            ?: Response(BAD_REQUEST)
    }

    private fun getAllLists(request: Request): Response {
        val user = userLens(request)

        return hub.getLists(user)
            ?.let { renderListsPage(user, it) }
            ?.let(::createResponse)
            ?: Response(BAD_REQUEST)
    }

    private fun createNewList(request: Request): Response {
        val user = userLens(request)
        val listName = request.extractListNameFromForm("listname")

        return listName
            ?.let { CreateToDoList(user, it) }
            ?.let(hub::handle)
            ?.let { Response(SEE_OTHER).with(Header.LOCATION of Uri.of("/todo/${user.name}")) }
            ?: Response(BAD_REQUEST)
    }

    private fun extractListData(request: Request): Pair<User, ListName> {
        return userLens(request) to listNameLens(request)
    }

    private fun fetchListContent(listId: Pair<User, ListName>): ToDoList =
        hub.getList(listId.first, listId.second)
            ?: error("List unknown")

    private fun createResponse(html: HtmlPage): Response = Response(OK).body(html.raw)
}

private fun Request.extractListNameFromForm(formName: String): ListName? =
    form(formName)?.let(ListName::fromUntrusted)
