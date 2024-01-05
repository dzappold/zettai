import java.time.LocalDate

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList?
    fun getLists(user: User): List<ListName>?
}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)
data class ListName internal constructor(val name: String) {
    companion object {
        private val validUrlPattern = "[A-Za-z0-9-]+".toRegex()
        fun fromTrusted(name: String): ListName = ListName(name)
        fun fromUntrusted(name: String): ListName? =
            if (name.matches(validUrlPattern) && name.length in 1..40)
                fromTrusted(name)
            else
                null
    }
}

data class User(val name: String)
data class ToDoItem(val description: String, val dueDate: LocalDate? = null, val status: ToDoStatus = ToDoStatus.Todo)
enum class ToDoStatus { Todo, InProgress, Done, Blocked }
