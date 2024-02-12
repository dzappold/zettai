package DDT.actions

import DDT.actors.ToDoListOwner
import InvalidRequestError
import ListName
import ToDoItem
import ToDoList
import commands.ToDoListCommandHandler
import events.ToDoListEventStore
import events.ToDoListEventStreamer
import events.ToDoListEventStreamerInMemory
import ToDoListFetcherFromMap
import ToDoListHub
import ToDoListStore
import ToDoStatus
import User
import Zettai
import ZettaiOutcome
import fp.asFailure
import fp.asSuccess
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.body.Form
import org.http4k.core.body.toBody
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import ui.HtmlPage
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_LOCAL_DATE

class HttpActions(env: String = "local") : ZettaiActions {
    private fun emptyStore(): ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(emptyStore())

    private val eventStreamer: ToDoListEventStreamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(eventStreamer)
    private val commandHandler = ToDoListCommandHandler(eventStore, fetcher)

    private val hub = ToDoListHub(fetcher, commandHandler, eventStore)
    override val protocol = Http(env)

    private val zettaiPort = 8000
    private val server = Zettai(hub).asServer(Jetty(zettaiPort))
    private val client = OkHttp()

    override fun prepare(): DomainSetUp {
        server.start()
        return Ready
    }

    override fun tearDown() =
        also { server.stop() }

    private fun callZettai(method: Method, path: String): Response =
        client(Request(method, "http://localhost:$zettaiPort/$path"))

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val name = ListName.fromTrusted(listName)

        createList(user, name)

        val created = items.mapNotNull { addListItem(user, name, ToDoItem(it)) }
        expectThat(created).hasSize(items.size)
    }

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> {
        val response = callZettai(GET, allUserListsUrl(user))
        expectThat(response.status).isEqualTo(OK)

        val html = HtmlPage(response.bodyString())
        val names = extractListNamesFromPage(html)
        return names.map(ListName.Companion::fromTrusted).asSuccess()
    }

    fun createList(user: User, listName: ListName) {
        val response = submitToZettai(allUserListsUrl(user), newListForm(listName))
        expectThat(response.status).isEqualTo(SEE_OTHER)
    }

    private fun newListForm(listName: ListName): Form {
        return listOf("listname" to listName.name)
    }

    private fun extractListNamesFromPage(html: HtmlPage): List<String> =
        html.parse()
            .select("tr")
            .mapNotNull { it.select("td").firstOrNull()?.text() }

    private fun allUserListsUrl(user: User): String = "todo/${user.name}"

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> {
        val response = callZettai(GET, todoListUrl(user, listName))

        if (response.status == NOT_FOUND)
            return InvalidRequestError("list $listName for $user not found").asFailure()

        expectThat(response.status).isEqualTo(OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items).asSuccess()
    }

    override fun addListItem(user: User, listName: ListName, item: ToDoItem) {
        val response = submitToZettai(
            todoListUrl(user, listName),
            listOf("itemname" to item.description, "itemdue" to item.dueDate?.toString())
        )
        expectThat(response.status).isEqualTo(SEE_OTHER)
    }

    private fun submitToZettai(path: String, webForm: Form): Response =
        client(Request(POST, "http://localhost:$zettaiPort/$path").body(webForm.toBody()))

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

    private fun extractItemsFromPage(html: HtmlPage): List<ToDoItem> =
        html.parse()
            .select("tr")
            .filter { element: Element -> element.select("td").size == 3 }
            .map {
                Triple(
                    it.select("td")[0].text().orEmpty(),
                    it.select("td")[1].text().toIsoLocalDate(),
                    it.select("td")[2].text().orEmpty().toStatus()
                )
            }
            .map { (name, dueDate, status) ->
                ToDoItem(name, dueDate, status)
            }

    private fun todoListUrl(user: User, listName: ListName) =
        "todo/${user.name}/${listName.name}"

    fun String?.toIsoLocalDate(): LocalDate? =
        unlessNullOrEmpty { LocalDate.parse(this, ISO_LOCAL_DATE) }

    fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
    fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
        if (isNullOrEmpty()) null else f(this)
}
