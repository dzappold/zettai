package zettai.store

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import zettai.commands.ToDoListId
import zettai.events.ItemAdded
import zettai.events.ItemModified
import zettai.events.ItemRemoved
import zettai.events.ListClosed
import zettai.events.ListCreated
import zettai.events.ListPutOnHold
import zettai.events.ListReleased
import zettai.events.ToDoListEvent
import zettai.expectSuccess
import zettai.randomItem
import zettai.randomListName
import zettai.randomText
import zettai.randomUser
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
