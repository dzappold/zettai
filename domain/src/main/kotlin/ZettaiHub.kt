import commands.ToDoListCommand
import java.time.LocalDate

interface ZettaiHub {
    fun handle(command: ToDoListCommand): ZettaiOutcome<ToDoListCommand>
    fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun getLists(user: User): ZettaiOutcome<List<ListName>>
    fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>>
}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)
data class ListName internal constructor(val name: String) {
    companion object {
        private val validUrlPattern = "[A-Za-z0-9-]+".toRegex()
        fun fromTrusted(name: String): ListName = ListName(name)

        fun fromUntrusted(name: String): ListName? =
            if (name.matches(validUrlPattern) && name.length in 1..40) fromTrusted(name) else null

        fun fromUntrustedOrThrow(name: String): ListName =
            fromUntrusted(name) ?: throw IllegalArgumentException("Invalid list name $name")
    }
}

data class User(val name: String)
data class ToDoItem(val description: String, val dueDate: LocalDate? = null, val status: ToDoStatus = ToDoStatus.Todo)
enum class ToDoStatus { Todo, InProgress, Done, Blocked }
