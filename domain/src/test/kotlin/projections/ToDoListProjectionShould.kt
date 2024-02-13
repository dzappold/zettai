package projections

import commands.ToDoListId
import events.ListCreated
import events.ToDoListEvent
import org.junit.jupiter.api.Test
import randomListName
import randomUser

class ToDoListProjectionShould {
    @Test
    fun `findAll returns all the lists of a user`() {
        val user = randomUser()
        val listName1 = randomListName()
        val listName2 = randomListName()

        val events = listOf(
            ListCreated(ToDoListId.mint(), user, listName1),
            ListCreated(ToDoListId.mint(), user, listName2),
            ListCreated(ToDoListId.mint(), randomUser(), randomListName()),
        )

        val projection = events.buildListProjection()

        projection.findAll(user)
    }
}

private fun List<ToDoListEvent>.buildListProjection(): ToDoListProjection =
    ToDoListProjection { after ->
        mapIndexed { index, e ->
            StoredEvent(EventSeq(after.progressive + index + 1), e)
        }.asSequence()
    }.also(ToDoListProjection::update)

