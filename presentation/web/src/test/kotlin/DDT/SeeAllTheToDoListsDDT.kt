package DDT

import DDT.actions.ZettaiDDT
import DDT.actions.allActions
import DDT.actors.ToDoListOwner
import com.ubertob.pesticide.core.DDT

class SeeAllTheToDoListsDDT : ZettaiDDT(allActions()) {
    val carol by NamedActor(::ToDoListOwner)
    val emma by NamedActor(::ToDoListOwner)
    val dylan by NamedActor(::ToDoListOwner)

    @DDT
    fun `new users have no lists`() = ddtScenario {
        play(
            emma.`cannot see any list`()
        )
    }

    @DDT
    fun `only owners can see all their lists`() = ddtScenario {
        val expectedLists = generateSomeToDoLists()
        setUp {
            carol.`starts with some lists`(expectedLists)
        }.thenPlay(
            carol.`can see the lists #listNames`(expectedLists.keys),
            emma.`cannot see any list`()
        )
    }

    @DDT
    fun `users can create new lists`() = ddtScenario {
        play(
            dylan.`cannot see any list`(),
            dylan.`can create a new list called #listname`("gardening"),
            dylan.`can create a new list called #listname`("music"),
            dylan.`can see the lists #listNames`(setOf("gardening", "music"))
        )
    }

    private fun generateSomeToDoLists(): Map<String, List<String>> =
        mapOf(
            "work" to listOf("meeting", "spreadsheet"),
            "home" to listOf("buy food"),
            "friends" to listOf("buy present", "book restaurant")
        )
}
