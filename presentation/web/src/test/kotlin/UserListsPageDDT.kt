import com.ubertob.pesticide.core.DDT

class UserListsPageDDT : ZettaiDDT(allActions()) {
    val carol by NamedActor(::ToDoListOwner)
    val emma by NamedActor(::ToDoListOwner)

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

    private fun generateSomeToDoLists(): Map<String, List<String>> {
        return mapOf(
            "work" to listOf("meeting", "spreadsheet"),
            "home" to listOf("buy food"),
            "friends" to listOf("buy present", "book restaurant")
        )
    }
}
