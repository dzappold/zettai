package fp

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FunctionsShould {
    @Test
    fun liftList() {
        val strFirstLifted = liftList(String::first)
        val words: List<String> = listOf(
            "Cuddly", "Acrobatic", "Tenacious", "Softly-purring"
        )

        val initials: List<Char> = strFirstLifted(words)
        initials shouldBe "CATS".toList()
    }
}
