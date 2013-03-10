package cucumber.runtime.scala.transform

import cucumber.api.Transformer
import cucumber.runtime.scala.model.Snake

/**
 * Converts the String taken from Cucumber into a Snake object
 */
class SnakeConverter extends Transformer[Snake] {

  /**
   * Transforms an ASCII snake into an object, for example:
   *
   * {{{
   *  ====>  becomes Snake(length = 5, direction = 'east)
   *    ==>  becomes Snake(length = 3, direction = 'east)
   * }}}
  */
  def transform(s:String):Snake = {
    val size = s.size
    val direction = s.toList match {
      case '<' :: _ => 'west
      case l if l.last == '>' => 'east
    }
    Snake(size, direction)
  }

}
