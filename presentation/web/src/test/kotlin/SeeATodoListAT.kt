import org.http4k.client.OkHttp
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Request
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.filter.ClientFilters
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.opentest4j.AssertionFailedError
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class SeeATodoListAT {
    val frank = ToDoListOwner("Frank")
    val foodToBuy = listOf("carrots", "apples", "milk")
    val frankList = createList("shopping", foodToBuy)

    val bob = ToDoListOwner("Bob")
    val gardenItems = listOf("fix the fence", "mowing the lawn")
    val bobList = createList("gardening", gardenItems)

    val lists = mapOf(
        frank.asUser() to listOf(frankList),
        bob.asUser() to listOf(bobList),
    )

    fun ToDoListOwner.asUser(): User = User(name)

    @Test
    fun `List owners can see their lists`() {
        val app = startTheApplication(lists)
        app.runScenario(
            frank.canSeeTheList("shopping", foodToBuy),
            bob.canSeeTheList("gardening", gardenItems)
        )
    }

    @Test
    fun `Only owners can see their lists`() {
        val app = startTheApplication(lists)
        app.runScenario(
            frank.cannotSeeTheList("gardening"),
            bob.cannotSeeTheList("shopping")
        )
    }
}

class ApplicationForAT(val client: HttpHandler, val server: AutoCloseable) : Actions {
    override fun getToDoList(user: String, listName: String): ToDoList {
        val client = OkHttp()
        val request = Request(GET, "http://localhost:8081/todo/$user/$listName")

        val response = client(request)
        return if (response.status == OK)
            parseResponse(response.toMessage())
        else
            fail(response.toMessage())
    }

    fun runScenario(vararg steps: Step) {
        server.use {
            steps.onEach { step -> step(this) }
        }
    }

    private fun parseResponse(html: String): ToDoList {
        val nameRegex = "<h2>.*<".toRegex()
        val listName = ListName(extractListName(nameRegex, html))
        val itemsRegex = "<td>.*?<".toRegex()
        val items = itemsRegex.findAll(html).map { ToDoItem(extractItemDescr(it)) }.toList()
        return ToDoList(listName, items)
    }

    private fun extractListName(nameRegex: Regex, html: String): String =
        nameRegex.find(html)?.value?.substringAfter("<h2>")?.dropLast(1).orEmpty()

    private fun extractItemDescr(matchResult: MatchResult): String =
        matchResult.value.substringAfter("<td>").dropLast(1)
}

private fun startTheApplication(lists: Map<User, List<ToDoList>>): ApplicationForAT {
    val server = Zettai(lists).asServer(Jetty(8081))
    server.start()

    val client = ClientFilters.SetBaseUriFrom(Uri.of("http://localhost:${server.port()}")).then(OkHttp())

    return ApplicationForAT(client, server)
}

interface ScenarioActor {
    val name: String
}

class ToDoListOwner(override val name: String) : ScenarioActor {
    fun canSeeTheList(listName: String, items: List<String>): Step = {
        val expectedList = createList(listName, items)

        val list = getToDoList(name, listName)

        expectThat(list).isEqualTo(expectedList)
    }

    fun cannotSeeTheList(listName: String): Step =
        { expectThrows<AssertionFailedError> { getToDoList(name, listName) } }
}

private fun createList(listName: String, items: List<String>) =
    ToDoList(ListName(listName), items.map(::ToDoItem))

interface Actions {
    fun getToDoList(user: String, listName: String): ToDoList?
}

typealias Step = Actions.() -> Unit
