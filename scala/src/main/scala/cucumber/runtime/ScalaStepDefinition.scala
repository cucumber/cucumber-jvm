package cucumber
package runtime

import cucumber.table.Table

import gherkin.formatter.model.Step
import gherkin.formatter.model.Row

import collection.JavaConverters._

import _root_.java.util.Locale
import _root_.java.util.regex.Pattern

class ScalaStepDefinition(frame:StackTraceElement, name:String, pattern:String, parameterTypes:List[Class[_]], f:List[Any] => Any) extends StepDefinition {

  private val argumentMatcher = new JdkPatternArgumentMatcher(Pattern.compile(pattern))

  def matchedArguments(step: Step) = argumentMatcher.argumentsFrom(step.getName)

  def getTypeForTableList(argIndex: Int) = null

  def getLocation = frame.getFileName + ":" + frame.getLineNumber

  // capture type transformations at compile time instead
  def getParameterTypes = Array.fill(parameterTypes.size)(classOf[String])

  def execute(args: Array[AnyRef]) { f(args.toList) }

  def isDefinedAt(stackTraceElement: StackTraceElement) = stackTraceElement == frame

  def getPattern = pattern
}