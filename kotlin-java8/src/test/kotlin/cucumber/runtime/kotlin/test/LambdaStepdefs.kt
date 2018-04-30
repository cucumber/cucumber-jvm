package cucumber.runtime.kotlin.test;

import cucumber.api.Scenario
import io.cucumber.datatable.DataTable
import cucumber.api.java8.En
import org.junit.Assert.*

var lastInstance : LambdaStepdefs? = null

class LambdaStepdefs : En {

    init {
        Before { scenario: Scenario ->
            assertNotSame(this, lastInstance)
            lastInstance = this
        }

        BeforeStep { scenario: Scenario ->
            assertSame(this, lastInstance)
            lastInstance = this
        }

        AfterStep { scenario: Scenario ->
            assertSame(this, lastInstance)
            lastInstance = this
        }

        After { scenario: Scenario ->
            assertSame(this, lastInstance)
            lastInstance = this
        }

        Given("this data table:") { peopleTable: DataTable ->
            val people : List<Person> = peopleTable.asList(Person::class.java)
            assertEquals("Aslak", people[0].first)
            assertEquals("Hellesøy", people[0].last)
        }

        val alreadyHadThisManyCukes = 1
        Given("I have {long} cukes in my belly") { n: Long ->
            assertEquals(1, alreadyHadThisManyCukes)
            assertEquals(42L, n)
        }

        val localState = "hello"
        Then("I really have {int} cukes in my belly") { i: Int ->
            assertEquals(42, i)
            assertEquals("hello", localState)
        }

        Given("A statement with a body expression$") { assertTrue(true) }

        Given("A statement with a simple match$", { -> assertTrue(true) })

        val localInt = 1
        Given("A statement with a scoped argument$", { assertEquals(2, localInt + 1) })

        Given("I will give you {int} and {float} and {word} and {int}") { a: Int, b: Float, c: String, d: Int ->
            assertEquals(1, a)
            assertEquals(2.2f, b)
            assertEquals("three", c)
            assertEquals(4, d)
        }
    }

    class Person {
        var first: String? = null
        var last: String? = null
    }
}
