import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class SeeATodoListAT {
    @Test
    fun `List owners can see their lists`() {
        val user = "frank"
        val listName = "shopping"
        val foodToBuy = listOf("carrots", "apples", "milk")

        startTheApplication(user, listName, foodToBuy)

        val list = getToDoList(user, listName)
        expectThat(list.listName.name).isEqualTo(listName)
        expectThat(list.items.map(ToDoItem::description)).isEqualTo(foodToBuy)
    }

}

fun getToDoList(user: String, listName: String): ToDoList {
    val client = OkHttp()
    val request = Request(Method.GET, "http://localhost:8081/todo/$user/$listName")

    val response = client(request)
    return if (response.status == Status.OK)
        parseResponse(response.toMessage())
    else
        fail(response.toMessage())
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

private fun startTheApplication(user: String, listName: String, items: List<String>) {
    val toDoList = ToDoList(ListName(listName), items.map(::ToDoItem))
    val lists = mapOf(User(user) to listOf(toDoList))
    val server = Zettai(lists).asServer(Jetty(8081))
    server.start()
}
