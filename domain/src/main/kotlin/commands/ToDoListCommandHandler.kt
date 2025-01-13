package commands

import InconsistentStateError
import ToDoListCommandError
import ZettaiOutcome
import events.ActiveToDoList
import events.ClosedToDoList
import events.InitialState
import events.ItemAdded
import events.ListCreated
import events.OnHoldToDoList
import events.ToDoListEvent
import events.ToDoListRetriever
import fp.asFailure
import fp.asSuccess

typealias CommandHandler<CMD, EVENT> = (CMD) -> ZettaiOutcome<List<EVENT>>
typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever,
) : CommandHandler<ToDoListCommand, ToDoListEvent> {
    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): ToDoListCommandOutcome {
        val listState = entityRetriever.retrieveByName(user, name) ?: InitialState
        return when (listState) {
            InitialState -> ListCreated(id, user, name).asCommandSuccess()
            is ActiveToDoList,
            is OnHoldToDoList,
            is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
        }
    }

    private fun AddToDoItem.execute(): ToDoListCommandOutcome =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    is ActiveToDoList -> {
                        if (listState.items.any { it.description == item.description })
                            ToDoListCommandError("cannot have 2 items with same name").asFailure()
                        else {
                            ItemAdded(listState.id, item).asCommandSuccess()
                        }
                    }

                    InitialState,
                    is OnHoldToDoList,
                    is ClosedToDoList -> InconsistentStateError(this, listState).asFailure()
                }
            } ?: ToDoListCommandError("list $name not found").asFailure()

}

private fun ToDoListEvent.asCommandSuccess(): ToDoListCommandOutcome = listOf(this).asSuccess()
