package cucumber.runtime.scala

import _root_.java.lang.reflect.Type
import _root_.gherkin.formatter.model.Step
import _root_.gherkin.I18n
import _root_.java.util.regex.Pattern
import _root_.cucumber.runtime.StepDefinition
import _root_.cucumber.runtime.JdkPatternArgumentMatcher
import _root_.cucumber.runtime.ParameterInfo
import collection.JavaConversions._

/**
 * Implementation of step definition for scala.
 *
 * @param frame Representation of a stack frame containing information about the context in which a
 *              step was defined. Allows retrospective queries about the definition of a step.
 *
 * @param name The name of the step definition class, e.g. cucumber.runtime.scala.test.CukesStepDefinitions
 *
 * @param pattern The regex matcher that defines the cucumber step, e.g. /I eat (.*) cukes$/

 * @param parameterInfos
 *
 * @param f Function body of a step definition. This is what actually runs the code within the step def.
 */
class ScalaStepDefinition(frame:StackTraceElement,
                          name:String,
                          pattern:String,
                          parameterInfos:List[Class[_]],
                          f:List[Any] => Any) extends StepDefinition {

  /**
   * Compiled pattern matcher for the cucumber step regex.
   */
  private val argumentMatcher = new JdkPatternArgumentMatcher(Pattern.compile(pattern))

  def matchedArguments(step: Step) = argumentMatcher.argumentsFrom(step.getName)

  def getLocation(detail: Boolean) = frame.getFileName + ":" + frame.getLineNumber

  def getParameterCount() = parameterInfos.size()

  def getParameterType(index: Int, javaType: Type) = new ParameterInfo(parameterInfos.get(index), null, null, null)

  def execute(i18n: I18n, args: Array[AnyRef]) { f(args.toList) }

  def isDefinedAt(stackTraceElement: StackTraceElement) = stackTraceElement == frame

  def getPattern = pattern

}
