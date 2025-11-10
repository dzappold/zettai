package zettai

import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.Status.Companion.UNPROCESSABLE_ENTITY
import org.http4k.core.body.form
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import zettai.commands.AddToDoItem
import zettai.commands.CreateToDoList
import zettai.fp.failIfNull
import zettai.fp.onFailure
import zettai.fp.recover
import zettai.fp.tryOrNull
import zettai.ui.HtmlPage
import zettai.ui.renderListPage
import zettai.ui.renderListsPage
import zettai.ui.renderWhatsNextPage
import java.time.LocalDate

class Zettai(val hub: ZettaiHub) : HttpHandler {
    override fun invoke(request: Request): Response = httpHandler(request)

    val httpHandler = routes(
        "/ping" bind GET to { Response(OK) },
        "/todo/{user}/{listname}" bind GET to ::getToDoList,
        "/todo/{user}/{listname}" bind POST to ::addNewItem,
        "/todo/{user}" bind GET to ::getAllLists,
        "/todo/{user}" bind POST to ::createNewList,
        "/whatsnext/{user}" bind GET to ::whatsNext
    )

    private fun createNewList(request: Request): Response {
        val user = request.extractUser().recover { User("anonymous") }
        val listName = request.form("listname")
            ?.let(ListName.Companion::fromUntrusted)
            ?: return Response(BAD_REQUEST).body("missing listname in form")

        return hub.handle(CreateToDoList(user, listName))
            .transform { Response(SEE_OTHER).header("Location", "/todo/${user.name}") }
            .recover { Response(UNPROCESSABLE_ENTITY).body(it.msg) }

    }

    private fun addNewItem(request: Request): Response {
        val user = request.extractUser().recover { User("anonymous") }
        val listName = request.extractListName().onFailure { return Response(BAD_REQUEST).body(it.msg) }
        val item = request.extractItem().onFailure { return Response(BAD_REQUEST).body(it.msg) }

        return hub.handle(AddToDoItem(user, listName, item))
            .transform { Response(SEE_OTHER).header("Location", "/todo/${user.name}/${listName.name}") }
            .recover { Response(UNPROCESSABLE_ENTITY).body(it.msg) }
    }

    private fun getToDoList(request: Request): Response {
        val user = request.extractUser().onFailure { return Response(BAD_REQUEST).body(it.msg) }
        val listName = request.extractListName().onFailure { return Response(BAD_REQUEST).body(it.msg) }

        return hub.getList(user, listName)
            .transform { renderListPage(user, it) }
            .transform(::toResponse)
            .recover { Response(NOT_FOUND).body(it.msg) }
    }

    fun toResponse(htmlPage: HtmlPage): Response =
        Response(OK).body(htmlPage.raw)

    private fun getAllLists(req: Request): Response {
        val user = req.extractUser().onFailure { return Response(BAD_REQUEST).body(it.msg) }

        return hub.getLists(user)
            .transform { renderListsPage(user, it) }
            .transform(::toResponse)
            .recover { Response(NOT_FOUND).body(it.msg) }
    }

    private fun whatsNext(req: Request): Response {
        val user = req.extractUser().onFailure { return Response(BAD_REQUEST).body(it.msg) }

        return hub.whatsNext(user)
            .transform { renderWhatsNextPage(user, it) }
            .transform(::toResponse)
            .recover { Response(NOT_FOUND).body(it.msg) }
    }

    private fun Request.extractUser(): ZettaiOutcome<User> =
        path("user")
            .failIfNull(InvalidRequestError("User not present"))
            .transform(::User)

    private fun Request.extractListName(): ZettaiOutcome<ListName> =
        path("listname")
            .orEmpty()
            .let(ListName.Companion::fromUntrusted)
            .failIfNull(InvalidRequestError("Invalid list name in path: $this"))

    private fun Request.extractItem(): ZettaiOutcome<ToDoItem> {
        val duedate = tryOrNull { LocalDate.parse(form("itemdue")) }
        return form("itemname")
            .failIfNull(InvalidRequestError("User not present"))
            .transform { ToDoItem(it, duedate) }
    }
}
