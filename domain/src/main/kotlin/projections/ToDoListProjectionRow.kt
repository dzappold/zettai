package zettai.projections

import zettai.ToDoItem
import zettai.ToDoList
import zettai.User

data class ToDoListProjectionRow(val user: User, val active: Boolean, val list: ToDoList) {
    fun addItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items + item))

    fun removeItem(item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - item))

    fun replaceItem(prevItem: ToDoItem, item: ToDoItem): ToDoListProjectionRow =
        copy(list = list.copy(items = list.items - prevItem + item))

    fun putOnHold(): ToDoListProjectionRow =
        copy(active = false)

    fun release(): ToDoListProjectionRow =
        copy(active = true)
}
