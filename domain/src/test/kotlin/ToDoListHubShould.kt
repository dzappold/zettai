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

fun randomUser() = User(randomString(lowercase, 3, 6).capitalize())

fun itemsGenerator(): Sequence<ToDoItem> = generateSequence {
    randomItem()
}

fun randomItem() = ToDoItem(randomString(lowercase + digits, 5, 20), null)


fun randomToDoList(): ToDoList = ToDoList(
    randomListName(),
    itemsGenerator().take(Random.nextInt(1, 6)).toList()
)


fun randomListName(): ListName = ListName(randomString(lowercase, 3, 6))

const val lowercase = "abcdefghijklmnopqrstuvwxyz"
const val digits = "0123456789"

fun randomString(charSet: String, minLen: Int, maxLen: Int) =
    StringBuilder().run {
        val len = if (maxLen > minLen) Random.nextInt(maxLen - minLen) + minLen else minLen
        repeat(len) {
            append(charSet.random())
        }
        toString()
    }
