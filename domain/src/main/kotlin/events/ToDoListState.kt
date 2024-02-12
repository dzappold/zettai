package events

import ListName
import ToDoItem
import User
import commands.EntityId
import commands.ToDoListId
import java.time.Instant

interface EntityState<in E : EntityEvent> {
    fun combine(event: E): EntityState<E>
}

sealed class ToDoListState : EntityState<ToDoListEvent> {
    abstract override fun combine(event: ToDoListEvent): ToDoListState
}

data object InitialState : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ListCreated -> create(event.id, event.owner, event.name, emptyList())
            else -> this // ignore other events
        }
}

data class ActiveToDoList internal constructor(
    val id: EntityId,
    val user: User,
    val name: ListName,
    val items: List<ToDoItem>
) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ItemAdded -> copy(items = items + event.item)
            is ItemRemoved -> copy(items = items - event.item)
            is ItemModified -> copy(items = items - event.prevItem + event.item)
            is ListPutOnHold -> onHold(event.reason)
            is ListClosed -> close(event.closedOn)
            else -> this // ignore other events
        }
}

data class OnHoldToDoList internal constructor(
    val id: EntityId,
    val user: User,
    val name: ListName,
    val itemList: List<ToDoItem>,
    val reason: String
) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        when (event) {
            is ListReleased -> release()
            else -> this // ignore other events
        }
}

data class ClosedToDoList internal constructor(val id: EntityId, val closedOn: Instant) : ToDoListState() {
    override fun combine(event: ToDoListEvent): ToDoListState =
        this // ignore other events
}

fun InitialState.create(id: ToDoListId, owner: User, name: ListName, items: List<ToDoItem>) =
    ActiveToDoList(id, owner, name, items)

fun ActiveToDoList.onHold(reason: String) =
    OnHoldToDoList(id, user, name, items, reason)

fun OnHoldToDoList.release() =
    ActiveToDoList(id, user, name, itemList)

fun ActiveToDoList.close(closedOn: Instant) =
    ClosedToDoList(id, closedOn)


fun Iterable<ToDoListEvent>.fold(): ToDoListState =
    fold(InitialState as ToDoListState) { acc, e -> acc.combine(e) }
