import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HttpActions(val env: String = "local") : ZettaiActions {
    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
    private val hub = ToDoListHub(lists)
    override val protocol = Http(env)

    val zettaiPort = 8000
    val server = Zettai(hub).asServer(Jetty(zettaiPort))
    val client = OkHttp()

    override fun prepare(): DomainSetUp {
        server.start()
        return Ready
    }

    override fun tearDown() =
        also { server.stop() }

    private fun callZettai(method: Method, path: String): Response =
        client(Request(method, "http://localhost:$zettaiPort/$path"))

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        lists += (this.user to listOf(ToDoList(ListName(listName), items.map(::ToDoItem))))
    }

    override fun getToDoList(user: User, listName: ListName): ToDoList? {
        val response = callZettai(Method.GET, todoListUrl(user, listName))

        if (response.status == Status.NOT_FOUND)
            return null

        expectThat(response.status).isEqualTo(Status.OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return ToDoList(listName, items)

    }

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

    private fun extractItemsFromPage(html: HtmlPage): List<ToDoItem> =
        html.parse()
            .select("tr")
//            .filter { it.select("td").size == 3 }
            .map {
//                Triple(
                it.select("td")[0].text().orEmpty()//,
//                    it.select("td")[1].text().toIsoLocalDate(),
//                    it.select("td")[2].text().orEmpty().toStatus()
//                )
            }
            .map { name ->
                ToDoItem(name)//, date, status)
            }

    private fun todoListUrl(user: User, listName: ListName) =
        "todo/${user.name}/${listName.name}"

    fun String?.toIsoLocalDate(): LocalDate? =
        unlessNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

    fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
    fun <U : Any> CharSequence?.unlessNullOrEmpty(f: (CharSequence) -> U): U? =
        if (isNullOrEmpty()) null else f(this)
}
