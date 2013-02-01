package cucumber.runtime.scala

import _root_.java.lang.reflect.Type
import _root_.gherkin.formatter.model.Step
import _root_.gherkin.I18n
import _root_.java.util.regex.Pattern
import _root_.cucumber.runtime.StepDefinition
import _root_.cucumber.runtime.JdkPatternArgumentMatcher
import _root_.cucumber.runtime.ParameterInfo
import collection.JavaConversions._

class ScalaStepDefinition(frame:StackTraceElement, name:String, pattern:String, parameterInfos:List[Class[_]], f:List[Any] => Any) extends StepDefinition {

  private val argumentMatcher = new JdkPatternArgumentMatcher(Pattern.compile(pattern))

  def matchedArguments(step: Step) = argumentMatcher.argumentsFrom(step.getName)

  def getLocation(detail: Boolean) = frame.getFileName + ":" + frame.getLineNumber

  def getParameterCount() = parameterInfos.size()

  // TODO: get rid of Transform.scala and leave transformation to be done by core. The correct implementation is commented out
  // below until this is fixed.
  // def getParameterType(index: Int, javaType: Type) = new ParameterInfo(parameterInfos.get(index), null)
  def getParameterType(index: Int, javaType: Type) = new ParameterInfo(parameterInfos.get(index), null, null, null)

  def execute(i18n: I18n, args: Array[AnyRef]) { f(args.toList) }

  def isDefinedAt(stackTraceElement: StackTraceElement) = stackTraceElement == frame

  def getPattern = pattern
}
