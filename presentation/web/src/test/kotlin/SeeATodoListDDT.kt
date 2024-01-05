import com.ubertob.pesticide.core.DDT

class SeeATodoListDDT : ZettaiDDT(allActions()) {
    val adam by NamedActor(::ToDoListOwner)
    val bob by NamedActor(::ToDoListOwner)
    val frank by NamedActor(::ToDoListOwner)
    val tom by NamedActor(::ToDoListOwner)

    val shoppingListName = "shopping"
    val shoppingItems = listOf("carrots", "apples", "milk")

    val gardeningListName = "gardening"
    val gardenItems = listOf("fix the fence", "mowing the lawn")

    @DDT
    fun `List owners can see their lists`() = ddtScenario {
        setUp {
            frank.`starts with a list`(shoppingListName, shoppingItems)
            bob.`starts with a list`(gardeningListName, gardenItems)
        }.thenPlay(
            frank.`can see #listname with #itemnames`(shoppingListName, shoppingItems),
            bob.`can see #listname with #itemnames`(gardeningListName, gardenItems),
        )
    }

    @DDT
    fun `Only owners can see their lists`() = ddtScenario {
        setUp {
            tom.`starts with a list`(shoppingListName, shoppingItems)
            adam.`starts with a list`(gardeningListName, gardenItems)
        }.thenPlay(
            tom.`cannot see #listname`(gardeningListName),
            adam.`cannot see #listname`(shoppingListName),
        )
    }
}

