typealias CommandHandler<CMD, EVENT> = (CMD) -> List<EVENT>?

class ToDoListCommandHandler(
    private val entityRetriever: ToDoListRetriever,
    private val readModel: ToDoListUpdatableFetcher // temporary
) : CommandHandler<ToDoListCommand, ToDoListEvent> {
    override fun invoke(command: ToDoListCommand): List<ToDoListEvent>? =
        when (command) {
            is CreateToDoList -> command.execute()
            else -> null
        }

    private fun CreateToDoList.execute(): List<ToDoListEvent>? =
        entityRetriever.retrieveByName(user, name)
            ?.let { listState ->
                when (listState) {
                    InitialState -> {
                        readModel.assignListToUser(user, ToDoList(name, emptyList()))
                        ListCreated(id, user, name).toList()
                    }
                    else -> null// command fail
                }
            }
}

private fun ToDoListEvent.toList(): List<ToDoListEvent> = listOf(this)
