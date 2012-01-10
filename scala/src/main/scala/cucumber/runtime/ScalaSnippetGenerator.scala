package cucumber.runtime

import gherkin.formatter.model.Step
import collection.JavaConverters._
import _root_.java.util.List

class ScalaSnippetGenerator(step: Step) extends SnippetGenerator(step: Step) {

  protected def template() =
    "{0}(\"\"\"{1}\"\"\")'{' ({3}) =>\n" +
      "  //// {4}\n" +
      "'}'"

  protected def arguments(argumentTypes: List[Class[_]]) = {
    val indexed = argumentTypes.asScala.zipWithIndex

    def name(clazz: Class[_]) =
      if(clazz.isPrimitive){
        val name = clazz.getName
        name.charAt(0).toUpper + name.substring(1)
      } else
        clazz.getSimpleName

    val named = indexed.map {
      case (c, i) => "arg" + i + ":" + name(c)
    }

    named.mkString(", ")
  }

}