package zettai.commands

import zettai.InconsistentStateError
import zettai.ToDoListCommandError
import zettai.ZettaiOutcome
import zettai.events.ActiveToDoList
import zettai.events.ClosedToDoList
import zettai.events.InitialState
import zettai.events.ItemAdded
import zettai.events.ListCreated
import zettai.events.OnHoldToDoList
import zettai.events.ToDoListEvent
import zettai.events.ToDoListRetriever
import zettai.fp.asFailure
import zettai.fp.asSuccess

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
