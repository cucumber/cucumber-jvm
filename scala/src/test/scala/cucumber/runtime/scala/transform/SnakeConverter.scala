package cucumber.runtime.scala.transform

import cucumber.api.Transformer
import cucumber.runtime.scala.model.Snake


class SnakeConverter extends Transformer[Snake] {

  def transform(s:String):Snake = {
    val size = s.size
    val direction = s.toList match {
      case '<' :: _ => 'west
      case l if l.last == '>' => 'east
    }
    Snake(size, direction)
  }

}
