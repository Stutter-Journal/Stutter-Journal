package at.isg.eloquia.tests.architecture

import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExampleUnitTest {

    private val dependency: Dependency = mockk()
    private val subject = Subject(dependency)

    @BeforeEach
    fun setUp() {
        // Reset mocks or state if needed, though creating fresh mocks per test is also fine
        // if using PER_CLASS and you want isolation, but user asked to create mocks once.
        // However, MockK mocks are mutable, so if we reuse them, we might need to clear them.
        // For this example, we'll just define behavior in the test.
        clearMocks(dependency)
    }

    @Nested
    inner class `When performing action` {

        @Test
        fun `it should return expected result`() {
            // Given
            every { dependency.getValue() } returns "Mocked Value"

            // When
            val result = subject.doSomething()

            // Then
            result shouldBe "Result: Mocked Value"
        }
    }

    // Example classes for the test
    class Dependency {
        fun getValue(): String = "Real Value"
    }

    class Subject(private val dependency: Dependency) {
        fun doSomething(): String = "Result: ${dependency.getValue()}"
    }
}
