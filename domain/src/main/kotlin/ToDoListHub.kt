import commands.ToDoListCommand
import commands.ToDoListCommandHandler
import events.EventPersister
import events.ToDoListEvent
import fp.failIfEmpty
import fp.failIfNull
import projections.ItemProjectionRow
import projections.ToDoListQueryRunner

class ToDoListHub(
    val queryRunner: ToDoListQueryRunner,
    val commandHandler: ToDoListCommandHandler,
    val persistEvents: EventPersister<ToDoListEvent>
) : ZettaiHub {
    override fun getList(user: User, listName: ListName): ZettaiOutcome<ToDoList> =
        queryRunner {
            listProjection.findList(user, listName)
                .failIfNull(InvalidRequestError("List $listName of user $user not found!"))
        }.runIt()

    override fun getLists(user: User): ZettaiOutcome<List<ListName>> =
        queryRunner {
            listProjection.findAll(user)
                .failIfNull(InvalidRequestError("User $user not found!"))
                .transform { it.toList() }
        }.runIt()

    override fun whatsNext(user: User) =
        queryRunner {
            listProjection.findAllActiveListId(user)
                .failIfEmpty(InvalidRequestError("User $user not found!"))
                .transform { userLists -> itemProjection.findWhatsNext(10, userLists) }
                .transform { itemProjectionRows -> itemProjectionRows.map(ItemProjectionRow::item) }
        }.runIt()

    override fun handle(command: ToDoListCommand) =
        commandHandler(command)
            .transform(persistEvents)
            .transform { command }
}

interface ToDoListFetcher {
    fun get(user: User, listName: ListName): ToDoList?
    fun getAll(user: User): List<ListName>?
}

fun ToDoList.replaceItem(item: ToDoItem) =
    items.filterNot { it.description == item.description } + item
