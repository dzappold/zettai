import java.util.concurrent.atomic.AtomicReference

typealias CommandHandler<CMD, EVENT> = (CMD) -> List<EVENT>?

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever
) : CommandHandler<ToDoListCommand, ToDoListEvent> {
    override fun invoke(command: ToDoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            else -> null
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    InitialState -> {
                        ListCreated(id, user, name).toList()
                    }
                    else -> null// command fail
                }
            }
}

private fun ToDoListEvent.toList(): List<ToDoListEvent> = listOf(this)

interface ToDoListRetriever {
    fun retrieveByName(user: User, name: ListName): ToDoListState?
}

typealias EventStreamer<E> = (EntityId) -> List<E>?
typealias EventPersister<E> = (List<E>) -> List<E>

class ToDoListEventStore(val eventStreamer: ToDoListEventStreamer) : ToDoListRetriever, EventPersister<ToDoListEvent> {
    private fun retrieveById(id: ToDoListId): ToDoListState? =
        eventStreamer(id)?.fold()

    override fun retrieveByName(user: User, name: ListName): ToDoListState? =
        eventStreamer.retrieveIdFromName(user, name)
            ?.let(::retrieveById)
            ?: InitialState

    override fun invoke(events: List<ToDoListEvent>): List<ToDoListEvent> =
        eventStreamer.store(events)

}

interface ToDoListEventStreamer : EventStreamer<ToDoListEvent> {
    fun retrieveIdFromName(user: User, listName: ListName): ToDoListId?
    fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent>
}

class ToDoListEventStreamerInMemory : ToDoListEventStreamer {
    val events = AtomicReference<List<ToDoListEvent>>(emptyList())

    override fun retrieveIdFromName(user: User, listName: ListName): ToDoListId? =
        events.get()
            .firstOrNull { it == ListCreated(it.id, user, listName) }
            ?.id

    override fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent> =
        newEvents.toList()
            .also { newEventList -> events.updateAndGet { it + newEventList } }

    override fun invoke(id: ToDoListId): List<ToDoListEvent> =
        events.get()
            .filter { it.id == id }
}
