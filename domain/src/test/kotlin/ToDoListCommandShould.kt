import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail

class ToDoListCommandShould {
    private val store: ToDoListStore = mutableMapOf()
    private val readModel: ToDoListUpdatableFetcher = ToDoListFetcherFromMap(store)

    private val streamer = ToDoListEventStreamerInMemory()
    private val eventStore = ToDoListEventStore(streamer)

    private val handler = ToDoListCommandHandler(eventStore, readModel)

    private fun handle(cmd: ToDoListCommand) =
        handler(cmd).transform(eventStore)

    @Test
    fun `CreateToDoList generate the correct event`() {
        val cmd = CreateToDoList(randomUser(), randomListName())

        val result: List<ToDoListEvent> = handler(cmd).expectSuccess()

        result shouldContain ListCreated(cmd.id, cmd.user, cmd.name)
    }

    @Test
    fun `Add list fails if the user has already a list with same name`() {
        val cmd = CreateToDoList(randomUser(), randomListName())
        val result = handle(cmd).expectSuccess().single()

        result.shouldBeInstanceOf<ListCreated>()

        val duplicated = handle(cmd).expectFailure()
        duplicated.shouldBeInstanceOf<InconsistentStateError>()
    }
}

fun <E : OutcomeError, T> Outcome<E, T>.expectSuccess(): T =
    onFailure { error -> fail { "$this expected success but was $error" } }


fun <E : OutcomeError, T> Outcome<E, T>.expectFailure(): E =
    onFailure { error -> return error }
        .let { fail { "Expected failure but was $it" } }
