package DDT.actions

import DDT.actors.ToDoListOwner
import com.ubertob.pesticide.core.DdtActions
import com.ubertob.pesticide.core.DdtProtocol
import com.ubertob.pesticide.core.DomainDrivenTest
import zettai.ListName
import zettai.ToDoItem
import zettai.ToDoList
import zettai.User
import zettai.ZettaiOutcome

interface ZettaiActions : DdtActions<DdtProtocol> {
    fun ToDoListOwner.`starts with a list`(listName: String, items: List<String>)
    fun ToDoListOwner.`starts with some lists`(lists: Map<String, List<String>>) =
        lists.forEach { (listName, items) ->
            `starts with a list`(listName, items)
        }

    fun getToDoList(user: User, listName: ListName): ZettaiOutcome<ToDoList>
    fun addListItem(user: User, listName: ListName, item: ToDoItem)
    fun allUserLists(user: User): ZettaiOutcome<List<ListName>>
    fun whatsNext(user: User): ZettaiOutcome<List<ToDoItem>>
}

typealias ZettaiDDT = DomainDrivenTest<ZettaiActions>

fun allActions() = setOf(
    DomainOnlyActions(),
    HttpActions("local")
)
