import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ToDoListHubShould {
    private fun emptyStore(): ToDoListStore = mutableMapOf()
    private val fetcher = ToDoListFetcherFromMap(emptyStore())
    private val hub = ToDoListHub(fetcher)

    @Test
    fun `get list by user and name`() {
        repeat(10) {
            val user = randomUser()
            val list = randomToDoList()

            fetcher.assignListToUser(user, list)

            val myList = hub.getList(user, list.listName)
            myList shouldBe list
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstUser = randomUser()
            val firstList = randomToDoList()

            val secondUser = randomUser()
            val secondList = randomToDoList()

            fetcher.assignListToUser(firstUser, firstList)
            fetcher.assignListToUser(secondUser, secondList)

            hub.getList(firstUser, secondList.listName).shouldBeNull()
            hub.getList(secondUser, firstList.listName).shouldBeNull()
        }
    }

}
