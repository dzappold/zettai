import com.ubertob.pesticide.core.DdtActor
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.map

data class ToDoListOwner(override val name: String) : DdtActor<ZettaiActions>() {
    val user = User(name)

    fun `cannot see #listname`(listName: String) =
        step(listName) {
            val list = getToDoList(user, ListName.fromTrusted(listName))
            expectThat(list).isNull()
        }


    fun `can see #listname with #itemnames`(listName: String, expectedItems: List<String>) =
        step(listName, expectedItems) {
            val list = getToDoList(user, ListName.fromTrusted(listName))

            expectThat(list)
                .isNotNull()
                .itemNames
                .containsExactlyInAnyOrder(expectedItems)
        }

    fun `can add #item to #listname`(itemName: String, listName: String) =
        step(itemName, listName) {
            val item = ToDoItem(itemName)
            addListItem(user, ListName.fromTrusted(listName), item)
        }

    fun `cannot see any list`() =
        step {
            val lists = allUserLists(user)
            expectThat(lists).isEmpty()
        }

    fun `can see the lists #listNames`(expectedLists: Set<String>) =
        step(expectedLists) {
            val lists = allUserLists(user)
            expectThat(lists)
                .map(ListName::name)
                .containsExactly(expectedLists)

        }

    fun `can create as new list called #listname`(listName: String) =
        step(listName) {
            createList(user, ListName.fromUntrusted(listName)!!)
        }

    private val Assertion.Builder<ToDoList>.itemNames
        get() = get { items.map(ToDoItem::description) }
}
