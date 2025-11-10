package zettai

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import zettai.commands.AddToDoItem
import zettai.commands.CreateToDoList

class ToDoListHubShould {
    val hub = prepareToDoListHubForTests()

    @Test
    fun `get list by user and name`() {
        usersGenerator().take(10).forEach { user ->
            val lists = toDoListsGenerator().take(100).toList()
            lists.forEach { list ->
                hub.handle(CreateToDoList(user, list.listName)).expectSuccess()
                list.items.forEach {
                    hub.handle(AddToDoItem(user, list.listName, it)).expectSuccess()
                }
            }

            lists.forEach { list ->
                val myList = hub.getList(user, list.listName).expectSuccess()
                myList.shouldBe(list)
            }
        }
    }

    @Test
    fun `don't get list from other users`() {
        repeat(10) {
            val firstList = randomToDoList()
            val secondList = randomToDoList()
            val firstUser = randomUser()
            val secondUser = randomUser()

            hub.getList(firstUser, secondList.listName).expectFailure().shouldBeInstanceOf<InvalidRequestError>()
            hub.getList(secondUser, firstList.listName).expectFailure().shouldBeInstanceOf<InvalidRequestError>()
        }
    }
}
