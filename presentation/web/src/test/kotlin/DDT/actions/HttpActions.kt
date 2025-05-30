package DDT.actions

import DDT.actors.ToDoListOwner
import DDT.actors.expectSuccess
import DDT.prepareZettaiForTests
import ListName
import ToDoItem
import ToDoList
import User
import ZettaiOutcome
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainSetUp
import com.ubertob.pesticide.core.Http
import com.ubertob.pesticide.core.NotReady
import com.ubertob.pesticide.core.Ready
import fp.asSuccess
import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.Status.Companion.SEE_OTHER
import org.http4k.core.body.Form
import org.http4k.core.body.toBody
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import ui.HtmlPage
import ui.toIsoLocalDate
import ui.toStatus
import java.time.Duration

data class HttpActions(val env: String = "local") : ZettaiActions {

    val zettaiPort = 8000 //different from the one in main

    val client = OkHttp()

    override val protocol: DdtProtocol = Http(env)
    override fun prepare(): DomainSetUp {
        if (verifyStarted(Duration.ZERO) == Ready)
            return Ready

        val server = prepareZettaiForTests().asServer(Jetty(zettaiPort))
        server.start()
        registerShutdownHook {
            server.stop()
        }
        return verifyStarted(Duration.ofSeconds(2))
    }


    private fun registerShutdownHook(hookToExecute: () -> Unit) {
        Runtime.getRuntime().addShutdownHook(Thread {
            val out = System.out
            try {
                hookToExecute()
            } finally {
                System.setOut(out)
            }
        })
    }

    override fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList> {

        val response = callZettai(GET, todoListUrl(user, listName))

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

    override fun allUserLists(user: User): ZettaiOutcome<List<ListName>> {
        val response = callZettai(GET, allUserListsUrl(user))

        expectThat(response.status).isEqualTo(OK)

        val html = HtmlPage(response.bodyString())

        val names = extractListNamesFromPage(html)

        return names.map { name -> ListName.fromTrusted(name) }.asSuccess()
    }

    override fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>> {
        val response = callZettai(GET, whatsNextUrl(user))

        expectThat(response.status).isEqualTo(OK)

        val html = HtmlPage(response.bodyString())

        val items = extractItemsFromPage(html)

        return items.asSuccess()
    }

    private fun newListForm(listName: ListName): Form = listOf("listname" to listName.name)

    override fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>) {
        val listName1 = ListName.fromTrusted(listName)
        val lists = allUserLists(user).expectSuccess()
        if (listName1 !in lists) {
            val response = submitToZettai(allUserListsUrl(user), newListForm(listName1))

            expectThat(response.status).isEqualTo(SEE_OTHER)  //redirect same page

            items.forEach {
                addListItem(user, listName1, ToDoItem(it))
            }

        }
    }


    private fun todoListUrl(user: User, listName: ListName) =
        "todo/${user.name}/${listName.name}"

    private fun allUserListsUrl(user: User) =
        "todo/${user.name}"

    private fun whatsNextUrl(user: User) =
        "whatsnext/${user.name}"

    private fun extractItemsFromPage(html: HtmlPage): List<ToDoItem> {
        return html.parse()
            .select("tr")
            .filter { element -> element.select("td").size == 3 }
            .map {
                Triple(
                    it.select("td")[0].text().orEmpty(),
                    it.select("td")[1].text().toIsoLocalDate(),
                    it.select("td")[2].text().orEmpty().toStatus()
                )
            }
            .map { (name, date, status) ->
                ToDoItem(name, date, status)
            }
    }

    private fun extractListNamesFromPage(html: HtmlPage): List<String> {
        return html.parse()
            .select("tr")
            .mapNotNull {
                it.select("td").firstOrNull()?.text()
            }
    }


    private fun verifyStarted(timeout: Duration): DomainSetUp {
        val begin = System.currentTimeMillis()
        while (true) {
            val r = callZettai(GET, "ping").status
            if (r == OK)
                return Ready
            if (elapsed(begin) >= timeout)
                return NotReady("timeout $timeout exceeded")
            Thread.sleep(10)
        }
    }

    private fun elapsed(since: Long): Duration =
        Duration.ofMillis(System.currentTimeMillis() - since)


    private fun submitToZettai(path: String, webForm: Form): Response =
        client(log(Request(POST, "http://localhost:$zettaiPort/$path").body(webForm.toBody())))

    private fun callZettai(method: Method, path: String): Response =
        client(log(Request(method, "http://localhost:$zettaiPort/$path")))

    fun <T> log(something: T): T {
        println("--- $something")
        return something
    }

    private fun HtmlPage.parse(): Document = Jsoup.parse(raw)

}
