package cucumber.runtime.scala.model

case class Person(name:String) {

  def hello = {
    "Hello, I'm " + name + "!"
  }

}
