package commands

import ListName
import ToDoItem
import User
import projections.RowId
import java.util.*

data class EntityId(val raw: UUID) {
    companion object {
        fun mint() = EntityId(UUID.randomUUID())
        fun fromRowId(rowId: RowId) = EntityId(UUID.fromString(rowId.id))
    }
}

typealias ToDoListId = EntityId

sealed class ToDoListCommand

data class CreateToDoList(val user: User, val name: ListName) : ToDoListCommand() {
    val id: ToDoListId = EntityId.mint()
}

data class AddToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()
