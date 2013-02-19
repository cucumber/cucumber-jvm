package cucumber.runtime.scala.model

import cucumber.runtime.scala.transform.SnakeConverter

/**
 * Test model "Snake" to exercise the custom mapper functionality
 * @param length of the snake in characters
 * @param direction in which snake is moving 'west, 'east, etc
 */
@cucumber.deps.com.thoughtworks.xstream.annotations.XStreamConverter(classOf[SnakeConverter])
case class Snake(length:Int, direction:Symbol) {

}
