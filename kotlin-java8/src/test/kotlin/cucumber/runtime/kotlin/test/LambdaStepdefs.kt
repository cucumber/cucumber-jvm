package cucumber.runtime.kotlin.test;

import cucumber.api.DataTable
import cucumber.api.Scenario
import cucumber.api.java8.En
import org.junit.Assert.*

var lastInstance : LambdaStepdefs? = null

class LambdaStepdefs : En {

    init {
        Before { scenario: Scenario ->
            assertNotSame(this, lastInstance)
            lastInstance = this
        }

        Given("^this data table:$") { peopleTable: DataTable ->
            val people = peopleTable.asList(Person::class.java)
            assertEquals("Aslak", people[0].first)
            assertEquals("Hellesøy", people[0].last)
        }

        val alreadyHadThisManyCukes = 1
        Given("^I have (\\d+) cukes in my belly$") { n: Long ->
            assertEquals(1, alreadyHadThisManyCukes)
            assertEquals(42L, n)
        }

        val localState = "hello"
        Then("^I really have (\\d+) cukes in my belly") { i: Int ->
            assertEquals(42, i)
            assertEquals("hello", localState)
        }

        Given("^A statement with a body expression$") { assertTrue(true) }

        Given("^A statement with a simple match$", { -> assertTrue(true) })

        val localInt = 1
        Given("^A statement with a scoped argument$", { assertEquals(2, localInt + 1) })

        Given("^I will give you (\\d+) and ([\\d\\.]+) and (\\w+) and (\\d+)$") { a: Int, b: Float, c: String, d: Int ->
            assertEquals(1, a)
            assertEquals(2.2f, b)
            assertEquals("three", c)
            assertEquals(4, d)
        }
    }

    class Person {
        internal var first: String? = null
        internal var last: String? = null
    }
}
