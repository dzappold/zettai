package zettai

import java.util.*
import kotlin.random.Random

fun stringsGenerator(charset: String, minLength: Int, maxLength: Int) =
    generateSequence {
        randomString(charset, minLength, maxLength)
    }

fun substituteRandomChar(fromCharset: String, intoString: String): String =
    intoString
        .toCharArray()
        .apply { set(Random.nextInt(intoString.length), fromCharset.random()) }
        .joinToString(separator = "")

fun usersGenerator(): Sequence<User> = generateSequence {
    randomUser()
}

fun randomUser() = User(randomString(lowercase, 3, 6).capitalize())
fun String.capitalize() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun itemsGenerator(): Sequence<ToDoItem> = generateSequence {
    randomItem()
}

fun randomItem() = ToDoItem(randomString(lowercase + digits, 5, 20), null)

fun toDoListsGenerator(): Sequence<ToDoList> = generateSequence {
    randomToDoList()
}

fun randomToDoList(): ToDoList = ToDoList(
    randomListName(),
    itemsGenerator().take(Random.nextInt(1, 6)).toList()
)

fun randomListName(): ListName = ListName.fromTrusted(randomString(lowercase, 5, 8))

const val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
const val lowercase = "abcdefghijklmnopqrstuvwxyz"
const val digits = "0123456789"
const val spacesigns = " ,.:+-()%$@"
const val text = lowercase + digits + spacesigns

fun randomString(charSet: String, minLen: Int, maxLen: Int) =
    buildString {
        val length = if (maxLen > minLen) Random.nextInt(minLen, maxLen) else minLen
        repeat(length) {
            append(charSet.random())
        }
    }

fun randomText(len: Int) = randomString(text, len, len)
