import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ToDoListCommandShould {
    val streamer = ToDoListEventStreamerInMemory()
    val eventStore = ToDoListEventStore(streamer)

    val handler = ToDoListCommandHandler(eventStore)

    fun handle(cmd: ToDoListCommand): List<ToDoListEvent>? =
        handler(cmd)?.let(eventStore)

    @Test
    fun `CreateToDoList generate the correct event`() {
        val cmd = CreateToDoList(randomUser(), randomListName())

        val result = handler(cmd)?.single()

        result shouldBe ListCreated(cmd.id, cmd.user, cmd.name)
    }

    @Test
    fun `Add list fails if the user has already a list with same name`() {
        val cmd = CreateToDoList(randomUser(), randomListName())
        val result = handle(cmd)?.single()

        result.shouldBeInstanceOf<ListCreated>()

        val duplicated = handle(cmd)
        duplicated.shouldBeNull()
    }
}
