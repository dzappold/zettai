package DDT

import DDT.actions.ZettaiDDT
import DDT.actions.allActions
import DDT.actors.ToDoListOwner
import com.ubertob.pesticide.core.DDT

class ModifyAToDoListDDT : ZettaiDDT(allActions()) {
    val ann by NamedActor(::ToDoListOwner)

    @DDT
    fun `The list owner can add new items`() = ddtScenario {
        setUp {
            ann.`starts with a list`("diy", emptyList())
        }.thenPlay(
            ann.`can add #item to #listname`("paint the shelf", "diy"),
            ann.`can add #item to #listname`("fix the gate", "diy"),
            ann.`can add #item to #listname`("change the lock", "diy"),
            ann.`can see #listname with #itemnames`("diy", listOf("paint the shelf", "fix the gate", "change the lock"))
        )
    }
}
