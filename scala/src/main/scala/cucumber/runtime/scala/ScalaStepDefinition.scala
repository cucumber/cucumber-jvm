package cucumber.runtime.scala

import _root_.java.lang.reflect.Type
import _root_.gherkin.formatter.model.Step
import _root_.gherkin.I18n
import _root_.java.util.regex.Pattern
import _root_.cucumber.runtime.StepDefinition
import _root_.cucumber.runtime.JdkPatternArgumentMatcher
import _root_.cucumber.runtime.ParameterInfo
import collection.JavaConversions._
import cucumber.api.Transform

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
                          parameterInfos:Array[Type],
                          f:List[Any] => Any) extends StepDefinition {

  /**
   * Compiled pattern matcher for the cucumber step regex.
   */
  private val argumentMatcher = new JdkPatternArgumentMatcher(Pattern.compile(pattern))

  /**
   * Returns a list of arguments. Return null if the step definition
   * doesn't match at all. Return an empty List if it matches with 0 arguments
   * and bigger sizes if it matches several.
   */
  def matchedArguments(step: Step) = argumentMatcher.argumentsFrom(step.getName)

  /**
   * The source line where the step definition is defined.
   * Example: foo/bar/Zap.brainfuck:42
   *
   * @param detail true if extra detailed location information should be included.
   */
  def getLocation(detail: Boolean) = frame.getFileName + ":" + frame.getLineNumber

  /**
   * How many declared parameters this stepdefinition has. Returns null if unknown.
   */
  def getParameterCount() = parameterInfos.size

  /**
   * The parameter type at index n. A hint about the raw parameter type is passed to make
   * it easier for the implementation to make a guess based on runtime information.
   * As Scala is a statically typed language, the javaType parameter is ignored
   */
  def getParameterType(index: Int, javaType: Type) = {
    new ParameterInfo(parameterInfos(index), null, null, null)
  }

  /**
   * Invokes the step definition. The method should raise a Throwable
   * if the invocation fails, which will cause the step to fail.
   */
  def execute(i18n: I18n, args: Array[AnyRef]) { f(args.toList) }

  /**
   * Return true if this matches the location. This is used to filter
   * stack traces.
   */
  def isDefinedAt(stackTraceElement: StackTraceElement) = stackTraceElement == frame

  /**
   * @return the pattern associated with this instance. Used for error reporting only.
   */
  def getPattern = pattern

  def isScenarioScoped = false
}
