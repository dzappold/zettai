package zettai.events

import zettai.ListName
import zettai.User
import zettai.commands.EntityId
import zettai.commands.ToDoListId
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

    override fun invoke(events: List<ToDoListEvent>): List<ToDoListEvent> =
        eventStreamer.store(events)
}

interface ToDoListEventStreamer : EventStreamer<ToDoListEvent> {
    fun retrieveIdFromName(user: User, listName: ListName): ToDoListId?
    fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent>
    fun fetchAfter(startEvent: EventSeq): Sequence<StoredEvent<ToDoListEvent>>
}

data class EventSeq(val progressive: Int) {
    operator fun compareTo(other: EventSeq): Int =
        progressive.compareTo(other.progressive)
}

data class StoredEvent<E : EntityEvent>(val eventSeq: EventSeq, val event: E)

typealias ToDoListStoredEvent = StoredEvent<ToDoListEvent>


class ToDoListEventStreamerInMemory : ToDoListEventStreamer {
    val events = AtomicReference<List<ToDoListStoredEvent>>(listOf())

    override fun retrieveIdFromName(user: User, listName: ListName): ToDoListId? =
        events.get()
            .map(ToDoListStoredEvent::event)
            .firstOrNull { it == ListCreated(it.id, user, listName) }
            ?.id

    override fun store(newEvents: Iterable<ToDoListEvent>): List<ToDoListEvent> =
        newEvents.toList().also { ne ->
            events.updateAndGet {
                it + ne.toSavedEvents(it.size)
            }
        }

    override fun invoke(id: ToDoListId): List<ToDoListEvent> =
        events.get()
            .map(ToDoListStoredEvent::event)
            .filter { it.id == id }

    override fun fetchAfter(startEvent: EventSeq): Sequence<ToDoListStoredEvent> =
        events.get()
            .asSequence()
            .dropWhile { it.eventSeq <= startEvent }

    private fun Iterable<ToDoListEvent>.toSavedEvents(last: Int): List<ToDoListStoredEvent> =
        mapIndexed { index, event ->
            ToDoListStoredEvent(EventSeq(last + index), event)
        }
}
