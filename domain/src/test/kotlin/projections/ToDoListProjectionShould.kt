import commands.ToDoListId
import events.EventSeq
import events.ItemAdded
import events.ItemModified
import events.ItemRemoved
import events.ListCreated
import events.StoredEvent
import events.ToDoListEvent
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import projections.ToDoListProjection

class ToDoListProjectionShould {
    @Test
    fun `findAll returns all the lists of a user`() {
        val user = randomUser()
        val listName1 = randomListName()
        val listName2 = randomListName()
        val events = listOf(
            ListCreated(ToDoListId.mint(), user, listName1),
            ListCreated(ToDoListId.mint(), user, listName2),
            ListCreated(ToDoListId.mint(), randomUser(), randomListName())
        )

        val projection = events.buildListProjection()

        projection.findAll(user).toList() shouldBe listOf(listName1, listName2)
    }

    @Test
    fun `findList get list with correct items`() {
        val user = randomUser()
        val listName = randomListName()
        val id = ToDoListId.mint()
        val item1 = randomItem()
        val item2 = randomItem()
        val item3 = randomItem()
        val events = listOf(
            ListCreated(id, user, listName),
            ItemAdded(id, item1),
            ItemAdded(id, item2),
            ItemModified(id, item2, item3),
            ItemRemoved(id, item1)
        )

        val projection = events.buildListProjection()

        projection.findList(user, listName) shouldBe ToDoList(listName, listOf(item3))
    }
}

private fun List<ToDoListEvent>.buildListProjection(): ToDoListProjection =
    ToDoListProjection { after ->
        mapIndexed { index, e ->
            StoredEvent(EventSeq(after.progressive + index + 1), e)
        }.asSequence()
    }.also(ToDoListProjection::update)
