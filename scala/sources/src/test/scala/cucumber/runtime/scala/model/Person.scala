package cucumber.runtime.scala.model

/**
 * Test model for a "Person"
 * @param name of person
 */
case class Person(name:String) {

  def hello = {
    "Hello, I'm " + name + "!"
  }

}
