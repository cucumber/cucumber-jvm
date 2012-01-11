package cucumber
package runtime

import gherkin.formatter.model.Step

import scala.collection.JavaConversions._

import _root_.java.util.regex.Pattern
import _root_.java.util.Locale

class ScalaStepDefinition(frame:StackTraceElement, name:String, pattern:String, parameterTypes:List[Class[_]], f:List[Any] => Any) extends StepDefinition {

  private val argumentMatcher = new JdkPatternArgumentMatcher(Pattern.compile(pattern))

  def matchedArguments(step: Step) = argumentMatcher.argumentsFrom(step.getName)

  def getTypeForTableList(argIndex: Int) = null

  def getLocation = frame.getFileName + ":" + frame.getLineNumber

  // capture type transformations at compile time instead
  def getParameterTypes = Array.fill(parameterTypes.size)(new ParameterType(classOf[String], null)).toList

  def execute(locale: Locale, args: Array[AnyRef]) { f(args.toList) }

  def isDefinedAt(stackTraceElement: StackTraceElement) = stackTraceElement == frame

  def getPattern = pattern
}
