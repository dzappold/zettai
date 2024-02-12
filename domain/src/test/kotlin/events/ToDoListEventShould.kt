package events

import commands.ToDoListId
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import randomItem
import randomListName
import randomUser

class ToDoListEventShould {
    val id = ToDoListId.mint()
    val name = randomListName()
    val user = randomUser()
    val item1 = randomItem()
    val item2 = randomItem()
    val item3 = randomItem()

    @Test
    fun `the first event create a list`() {
        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name)
        )

        val list = events.fold()

        list shouldBe ActiveToDoList(id, user, name, emptyList())
    }

    @Test
    fun `adding and removing items to active list`() {
        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemAdded(id, item3),
            ItemRemoved(id, item2)
        )

        val list = events.fold()

        list shouldBe ActiveToDoList(id, user, name, listOf(item1, item3))
    }

    @Test
    fun `putting the list on hold`() {
        val reason = "not urgent anymore"
        val events: List<ToDoListEvent> = listOf(
            ListCreated(id, user, name),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemAdded(id, item3),
            ListPutOnHold(id, reason)
        )

        val list = events.fold()

        list shouldBe OnHoldToDoList(id, user, name, listOf(item1, item2, item3), reason)
    }
}
