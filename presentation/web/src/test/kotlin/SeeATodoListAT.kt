import org.http4k.client.OkHttp
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
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
        parseResponse(response)
    else
        fail(response.toMessage())
}

private fun parseResponse(response: Response): ToDoList {
    TODO("parse the response")
}

private fun startTheApplication(user: String, listName: String, items: List<String>) {
    val server = Zettai().asServer(Jetty(8081)).start() // a random port
    // todo setup user and list
}
