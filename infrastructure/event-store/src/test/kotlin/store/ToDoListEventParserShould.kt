package store

import commands.ToDoListId
import events.ItemAdded
import events.ItemModified
import events.ItemRemoved
import events.ListClosed
import events.ListCreated
import events.ListPutOnHold
import events.ListReleased
import events.ToDoListEvent
import expectSuccess
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import randomItem
import randomListName
import randomText
import randomUser
import java.time.Instant

class ToDoListEventParserShould {
    val eventParser = toDoListEventParser()

    @Test
    fun `convert events to and from`() {
        eventsGenerator().take(100).forEach { event ->
            val conversion = eventParser.render(event)
            val newEvent = eventParser.parse(conversion).expectSuccess()

            newEvent shouldBe event
        }
    }
}

fun randomEvent(): ToDoListEvent =
    when (val kClass = ToDoListEvent::class.sealedSubclasses.random()) {
        ListCreated::class -> ListCreated(ToDoListId.mint(), randomUser(), randomListName())
        ItemAdded::class -> ItemAdded(ToDoListId.mint(), randomItem())
        ItemRemoved::class -> ItemRemoved(ToDoListId.mint(), randomItem())
        ItemModified::class -> ItemModified(ToDoListId.mint(), randomItem(), randomItem())
        ListPutOnHold::class -> ListPutOnHold(ToDoListId.mint(), randomText(20))
        ListReleased::class -> ListReleased(ToDoListId.mint())
        ListClosed::class -> ListClosed(ToDoListId.mint(), Instant.now())
        else -> error("Unexpected class: $kClass")
    }

fun eventsGenerator(): Sequence<ToDoListEvent> = generateSequence {
    randomEvent()
}
