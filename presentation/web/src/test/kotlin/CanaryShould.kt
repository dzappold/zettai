import io.kotest.matchers.ints.shouldBePositive
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.MethodOrderer.Random
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@TestMethodOrder(Random::class)
@Execution(ExecutionMode.CONCURRENT)
class CanaryShould {
    @ParameterizedTest
    @ValueSource(ints = [1])
    internal fun `ex plore`(value: Int) {
        value shouldBe 1
        1.shouldBePositive()
    }
}
