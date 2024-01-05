import java.util.*

data class EntityId(val raw: UUID) {
    companion object {
        fun mint(): EntityId = EntityId(UUID.randomUUID())
    }
}

typealias ToDoListId = EntityId

sealed class ToDoListCommand

data class CreateToDoList(val user: User, val name: ListName) : ToDoListCommand() {
    val id: ToDoListId = ToDoListId.mint()
}

data class AddToDoItem(val user: User, val name: ListName, val item: ToDoItem) : ToDoListCommand()
