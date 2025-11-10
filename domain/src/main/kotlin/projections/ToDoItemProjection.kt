package zettai.projections

import zettai.ToDoItem
import zettai.ToDoStatus
import zettai.commands.EntityId
import zettai.events.ItemAdded
import zettai.events.ItemModified
import zettai.events.ItemRemoved
import zettai.events.ListClosed
import zettai.events.ListCreated
import zettai.events.ListPutOnHold
import zettai.events.ListReleased
import zettai.events.ToDoListEvent

data class ItemProjectionRow(val item: ToDoItem, val listId: EntityId)

class ToDoItemProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ItemProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        ::eventProjector,
        eventFetcher
    ) {

    fun findWhatsNext(maxRows: Int, lists: List<EntityId>): List<ItemProjectionRow> =
        allRows().values
            .filter { it.listId in lists }
            .filter { it.item.dueDate != null && it.item.status == ToDoStatus.Todo }
            .sortedBy { it.item.dueDate }
            .take(maxRows)

    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ItemProjectionRow>> =
            when (e) {
                is ListCreated -> emptyList()
                is ItemAdded -> CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)).toSingle()
                is ItemRemoved -> DeleteRow<ItemProjectionRow>(e.itemRowId(e.item)).toSingle()
                is ItemModified -> listOf(
                    CreateRow(e.itemRowId(e.item), ItemProjectionRow(e.item, e.id)),
                    DeleteRow(e.itemRowId(e.prevItem))
                )

                is ListPutOnHold -> emptyList()
                is ListReleased -> emptyList()
                is ListClosed -> emptyList()
            }
    }
}

private fun ToDoListEvent.itemRowId(item: ToDoItem): RowId = RowId("${id}_${item.description}")
