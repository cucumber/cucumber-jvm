package cucumber.runtime.scala

import _root_.cucumber.runtime.snippets.Snippet
import _root_.gherkin.pickles.PickleStep
import _root_.java.util.List
import collection.JavaConverters._

class ScalaSnippetGenerator extends Snippet {

  def template() =
    "{0}(\"\"\"{1}\"\"\")'{' ({3}) =>\n" +
      "  //// {4}\n" +
      "  throw new PendingException()\n" +
      "'}'"

  def tableHint() = null

  def arguments(argumentTypes: List[Class[_]]) = {
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

  def namedGroupStart() = null

  def namedGroupEnd() = null

  def escapePattern(pattern:String) = pattern

}
