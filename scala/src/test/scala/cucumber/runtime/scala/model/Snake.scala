package cucumber.runtime.scala.model

import cucumber.runtime.scala.transform.SnakeConverter

@cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter(classOf[SnakeConverter])
case class Snake(length:Int, direction:Symbol) {

}
