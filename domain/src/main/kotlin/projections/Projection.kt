package projections

import events.EntityEvent

interface Projection<ROW : Any, EVENT : EntityEvent> {
    val eventProjector: ProjectEvents<ROW, EVENT>
    val eventFetcher: FetchStoredEvents<EVENT>

    fun lastProjectedEvent(): EventSeq

    fun update() {
        eventFetcher(lastProjectedEvent())
            .forEach { storedEvent ->
                applyDelta(storedEvent.eventSeq, eventProjector(storedEvent.event))
            }
    }

    fun applyDelta(eventSeq: EventSeq, deltas:List<DeltaRow<ROW>>)
}

data class EventSeq(val progressive: Int) {
    operator fun compareTo(other: EventSeq): Int =
        progressive.compareTo(other.progressive)
}

data class StoredEvent<E : EntityEvent>(val eventSeq: EventSeq, val event: E)

typealias FetchStoredEvents<E> = (EventSeq) -> Sequence<StoredEvent<E>>

typealias ProjectEvents<R, E> = (E) -> List<DeltaRow<R>>

data class RowId(val id: String)

sealed class DeltaRow<R : Any>

data class CreateRow<R : Any>(val rowId: RowId, val row: R) : DeltaRow<R>()
data class DeleteRow<R : Any>(val rowId: RowId) : DeltaRow<R>()
data class UpdateRow<R : Any>(val rowId: RowId, val updateRow: R.() -> R) : DeltaRow<R>()
