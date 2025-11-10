package zettai.events

import zettai.ListName
import zettai.ToDoItem
import zettai.User
import zettai.commands.EntityId
import zettai.commands.ToDoListId
import java.time.Instant

interface EntityEvent {
    val id: EntityId
}

sealed class ToDoListEvent : EntityEvent
data class ListCreated(override val id: ToDoListId, val owner: User, val name: ListName) : ToDoListEvent()
data class ItemAdded(override val id: ToDoListId, val item: ToDoItem) : ToDoListEvent()
data class ItemRemoved(override val id: ToDoListId, val item: ToDoItem) : ToDoListEvent()
data class ItemModified(override val id: ToDoListId, val prevItem: ToDoItem, val item: ToDoItem) : ToDoListEvent()
data class ListPutOnHold(override val id: ToDoListId, val reason: String) : ToDoListEvent()
data class ListReleased(override val id: ToDoListId) : ToDoListEvent()
data class ListClosed(override val id: ToDoListId, val closedOn: Instant) : ToDoListEvent()
