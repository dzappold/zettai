package ui

import ToDoItem
import ToDoStatus
import unlessNullOrEmpty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class HtmlPage(val raw: String)


internal fun List<ToDoItem>.renderItems() =
    joinToString(separator = "", transform = ::renderItem)

private fun renderItem(it: ToDoItem): String = """<tr>
              <td>${it.description}</td>
              <td>${it.dueDate?.toIsoString().orEmpty()}</td>
              <td>${it.status}</td>
            </tr>""".trimIndent()


fun LocalDate.toIsoString(): String = format(DateTimeFormatter.ISO_LOCAL_DATE)

fun String?.toIsoLocalDate(): LocalDate? =
    this.unlessNullOrEmpty { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }

fun String.toStatus(): ToDoStatus = ToDoStatus.valueOf(this)
