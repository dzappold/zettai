package projections

import ListName
import ToDoList
import User
import commands.EntityId
import commands.ToDoListId
import events.ItemAdded
import events.ItemModified
import events.ItemRemoved
import events.ListClosed
import events.ListCreated
import events.ListPutOnHold
import events.ListReleased
import events.ToDoListEvent

class ToDoListProjection(eventFetcher: FetchStoredEvents<ToDoListEvent>) :
    InMemoryProjection<ToDoListProjectionRow, ToDoListEvent> by ConcurrentMapProjection(
        ::eventProjector,
        eventFetcher
    ) {

    fun findAll(user: User): Sequence<ListName> =
        allRows().values
            .asSequence()
            .filter { it.user == user }
            .map { it.list.listName }


    fun findList(user: User, name: ListName): ToDoList? =
        allRows().values
            .firstOrNull { it.user == user && it.list.listName == name }
            ?.list

    fun findAllActiveListId(user: User): List<EntityId> =
        allRows()
            .filter { it.value.user == user && it.value.active }
            .map { ToDoListId.fromRowId(it.key) }


    companion object {
        fun eventProjector(e: ToDoListEvent): List<DeltaRow<ToDoListProjectionRow>> =
            when (e) {
                is ListCreated -> CreateRow(
                    e.rowId(),
                    ToDoListProjectionRow(e.owner, true, ToDoList(e.name, emptyList()))
                )

                is ItemAdded -> UpdateRow<ToDoListProjectionRow>(e.rowId()) { addItem(e.item) }
                is ItemRemoved -> UpdateRow(e.rowId()) { removeItem(e.item) }
                is ItemModified -> UpdateRow(e.rowId()) { replaceItem(e.prevItem, e.item) }
                is ListPutOnHold -> UpdateRow(e.rowId()) { putOnHold() }
                is ListReleased -> UpdateRow(e.rowId()) { release() }
                is ListClosed -> DeleteRow(e.rowId())
            }.toSingle()
    }
}

private fun ToDoListEvent.rowId(): RowId = RowId(id.raw.toString())
