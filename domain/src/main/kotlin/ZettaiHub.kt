import java.time.LocalDate

interface ZettaiHub {
    fun getList(user: User, listName: ListName): ToDoList?
    fun addItemToList(user: User, listName: ListName, item: ToDoItem): ToDoList?
}

data class ToDoList(val listName: ListName, val items: List<ToDoItem>)
data class ListName(val name: String)
data class User(val name: String)
data class ToDoItem(val description: String, val dueDate: LocalDate? = null)
enum class ToDoStatus { Todo, InProgress, Done, Blocked }
