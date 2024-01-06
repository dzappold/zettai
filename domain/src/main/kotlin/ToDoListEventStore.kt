import java.util.concurrent.atomic.AtomicReference

typealias EventStreamer<E> = (EntityId) -> List<E>?
typealias EventPersister<E> = (List<E>) -> List<E>

interface ToDoListRetriever {
    fun retrieveByName(user: User, name: ListName): ToDoListState?
}

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
    private val events = AtomicReference<List<ToDoListEvent>>(emptyList())

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
