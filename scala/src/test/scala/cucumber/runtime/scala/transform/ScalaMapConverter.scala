package cucumber.runtime.scala.transform

import cucumber.api.Transformer
import scala.collection

/**
 * Example converter for Scala Map class
 */
class ScalaMapConverter extends Transformer[Map[String, String]] {

  def transform(s:String):Map[String, String] = {
    s.replaceAll("[{}]", "")
      .split(",")
      .map(_.split("=>"))
      .map(_.toList)
      .map({case List(k,v) => Map(k -> v)})
      .reduce(_ ++ _)
  }

}
