package zettai

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ListNameShould {
    val validCharset = uppercase + lowercase + digits + "-"
    val invalidCharset = " !@#$%^&*()_+={}[]|:;'<>,./?\u2202\u2203\u2204\u2205"

    @Test
    fun `Valid names are alphanum+hyphen between 3 and 40 chars length`() {
        stringsGenerator(validCharset, 3, 40)
            .take(100)
            .forEach { ListName.fromUntrusted(it) shouldBe ListName.fromTrusted(it) }
    }

    @Test
    fun `Name cannot be empty`() {
        ListName.fromUntrusted("").shouldBeNull()
    }

    @Test
    fun `Names longer than 40 chars are not valid`() {
        stringsGenerator(validCharset, 41, 200)
            .take(100)
            .forEach { ListName.fromUntrusted(it).shouldBeNull() }
    }

    @Test
    fun `Invalid chars are not allowed in the name`() {
        stringsGenerator(validCharset, 1, 40)
            .map { substituteRandomChar(invalidCharset, it) }
            .take(1000)
            .forEach { ListName.fromUntrusted(it).shouldBeNull() }
    }
}
