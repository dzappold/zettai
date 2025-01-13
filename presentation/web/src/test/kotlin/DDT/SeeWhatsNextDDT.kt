package DDT

import DDT.actions.ZettaiDDT
import DDT.actions.allActions
import DDT.actors.ToDoListOwner
import com.ubertob.pesticide.core.DDT
import java.time.LocalDate

class SeeWhatsNextDDT : ZettaiDDT(allActions()) {
    val alice by NamedActor(::ToDoListOwner)

    @DDT
    fun `What's next show the items in order of urgency`() = ddtScenario {
        val gardenList = "gardening"
        val gardenTasks = listOf("mulching", "trim hedge")
        val partyList = "party"
        val partyTasks = listOf("cake", "decoration")

        setUp {
            alice.`starts with some lists`(mapOf(gardenList to gardenTasks, partyList to partyTasks))
        }.thenPlay(
            alice.`can see that #itemname is the next task to do`(""),
            alice.`can add #itemname to the #listname due to #duedate`(
                "prepare dress",
                partyList,
                LocalDate.now().plusDays(3)
            ),
            alice.`can add #itemname to the #listname due to #duedate`(
                "buy present",
                partyList,
                LocalDate.now().plusDays(2)
            ),
            alice.`can add #itemname to the #listname due to #duedate`(
                "go party",
                partyList,
                LocalDate.now().plusDays(4)
            ),
            alice.`can see that #itemname is the next task to do`("buy present"),
            alice.`can add #itemname to the #listname due to #duedate`(
                "water plants",
                gardenList,
                LocalDate.now().plusDays(1)
            ),
            alice.`can see that #itemname is the next task to do`("water plants")
        )
    }
}
