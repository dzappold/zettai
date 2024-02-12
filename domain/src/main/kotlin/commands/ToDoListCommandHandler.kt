package commands

import events.ActiveToDoList
import InconsistentStateError
import events.InitialState
import events.ItemAdded
import events.ListCreated
import ToDoList
import events.ToDoListEvent
import events.ToDoListRetriever
import ToDoListUpdatableFetcher
import ZettaiOutcome
import fp.asFailure
import fp.asSuccess

typealias CommandHandler<CMD, EVENT> = (CMD) -> ZettaiOutcome<List<EVENT>>
typealias ToDoListCommandOutcome = ZettaiOutcome<List<ToDoListEvent>>

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever,
    private val readModel: ToDoListUpdatableFetcher // temporary
) : CommandHandler<ToDoListCommand, ToDoListEvent> {
    override fun invoke(command: ToDoListCommand): ToDoListCommandOutcome =
        when (command) {
            is CreateToDoList -> command.execute()
            is AddToDoItem -> command.execute()
        }

    private fun CreateToDoList.execute(): ToDoListCommandOutcome {
        val listState = entityRetriever.retrieveByName(user, name) ?: InitialState
        return when (listState) {
            InitialState -> {
                readModel.assignListToUser(user, ToDoList(name, emptyList()))
                ListCreated(id, user, name).asCommandSuccess()
            }

            else -> InconsistentStateError(this, listState).asFailure()
        }
    }

    private fun AddToDoItem.execute(): ToDoListCommandOutcome {
        val listState = entityRetriever.retrieveByName(user, name) ?: InitialState
        return when (listState) {
            is ActiveToDoList -> {
                if (listState.items.any { it.description == item.description })
                    InconsistentStateError(this, listState).asFailure()
                else {
                    readModel.addItemToList(user, listState.name, item)
                    ItemAdded(listState.id, item).asCommandSuccess()
                }
            }

            else -> InconsistentStateError(this, listState).asFailure()
        }
    }
}

private fun ToDoListEvent.asCommandSuccess(): ToDoListCommandOutcome = listOf(this).asSuccess()
