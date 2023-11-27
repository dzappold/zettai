import com.ubertob.pesticide.core.DDT
import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtActor
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import com.ubertob.pesticide.core.DomainOnly
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.Ready
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

interface ZettaiActions : DdtActions<DdtProtocol> {
    fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>)

    fun getToDoList(user: User, listName: ListName): ToDoList?
}

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions("local")
)

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
        val response = callZettai(GET, todoListUrl(user, listName))

        if (response.status == NOT_FOUND)
            return null

        expectThat(response.status).isEqualTo(OK)

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

class DomainOnlyActions : ZettaiActions {
    override val protocol = DomainOnly
    override fun prepare() = Ready

    private val lists: MutableMap<User, List<ToDoList>> = mutableMapOf()
    private val hub by lazy { ToDoListHub(lists) }
    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        lists += (this.user to listOf(ToDoList(ListName(listName), items.map(::ToDoItem))))
    }

    override fun getToDoList(user: User, listName: ListName): ToDoList? =
        hub.getList(user, listName)
}

class SeeATodoListDDT : ZettaiDDT(allActions()) {
    val adam by NamedActor(::ToDoListOwner)
    val bob by NamedActor(::ToDoListOwner)
    val frank by NamedActor(::ToDoListOwner)
    val tom by NamedActor(::ToDoListOwner)

    val shoppingListName = "shopping"
    val shoppingItems = listOf("carrots", "apples", "milk")

    val gardeningListName = "gardening"
    val gardenItems = listOf("fix the fence", "mowing the lawn")

    @DDT
    fun `List owners can see their lists`() = ddtScenario {
        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardeningListName, gardenItems)
        }.thenPlay(
            frank.`can see #listname with #itemnames`(shoppingListName, shoppingItems),
            bob.`can see #listname with #itemnames`(gardeningListName, gardenItems),
        )
    }

    @DDT
    fun `Only owners can see their lists`() = ddtScenario {
        setUp {
            tom.`starts with a list`(shoppingListName, shoppingItems)
            adam.`starts with a list`(gardeningListName, gardenItems)
        }.thenPlay(
            tom.`cannot see #listname`(gardeningListName),
            adam.`cannot see #listname`(shoppingListName),
        )
    }
}

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    val user = User(name)

    fun `cannot see #listname`(listName: String) =
        step(listName) {
            val list = getToDoList(user, ListName(listName))
            expectThat(list).isNull()
        }


    fun `can see #listname with #itemnames`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName(listName))

            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map(ToDoItem::description) }
}
