package cuke4duke.examples

import collection.mutable.Map
import cuke4duke.{Table, ScalaDsl}
import java.util.{ArrayList, Arrays, List => JList, Map => JMap, HashMap => JHashMap}
import org.junit.Assert.{assertEquals, fail}

class DemoSteps extends ScalaDsl {
  val cukes = Map[String, Int]()

  Given("a pending step"){
    pending
    fail("We shouldn't get here because we are 'pending'")
  }

  Given("a failing step that is preceded by a pending"){
    fail("We shouldn't get here either because the previous one is pending")
  }

  Given("""I have (\d+) (.*) cukes""") { (n: Int, color: String) =>
    cukes += color -> n
  }

  When("I add a table") { table: Table =>
    val diffList = new ArrayList[JList[String]]();
    diffList.add(Arrays.asList("a", "b"));
    diffList.add(Arrays.asList("1", "2"));
    table.diffLists(diffList);

    val hashes = new ArrayList[JMap[String, String]]();
    hashes.add(hash("a" -> "1", "b" -> "2"));
    hashes.add(hash("a" -> "1", "b" -> "2"));

    val options = new JHashMap[String, Boolean]();
    options.put("surplus_row", false);
    table.diffHashes(hashes, options);
  }

  When("^I add a string$"){ s:String =>
    assertEquals("Hello\nWorld", s)
  }

  Then("I should have (\\d+) (.*) cukes"){ (n:Int, color:String) =>
    assertEquals(n, cukes.getOrElse(color, 0))
  }

  //

  def hash(pairs:(String, String)*):JMap[String,String] = {
    val m = new JHashMap[String, String]
    pairs.foreach{ case (a,b) => m.put(a, b) }
    m
  }
}