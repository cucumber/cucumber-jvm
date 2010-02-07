package cuke4duke.examples

import cuke4duke.Table
import cuke4duke.ScalaDsl
import org.junit.Assert.assertEquals

class MultilineSteps extends ScalaDsl {
  Given("^I have a table:$") { table: Table =>
    assertEquals("1", table.hashes().get(0).get("foo"))
    assertEquals("4", table.hashes().get(1).get("bar"))
  }

  Given("^I have a string:$") { string: String =>
    assertEquals("foo\nbar", string)
  }
}