package cuke4duke.internal.scala

import _root_.java.util.regex.Pattern
import cuke4duke.internal.language.{AbstractProgrammingLanguage, JdkPatternArgumentMatcher, StepDefinition}

class ScalaStepDefinition(name: String, val regexp_source: String, f: List[Any] => Any, signature: String, programmingLanguage: AbstractProgrammingLanguage) extends StepDefinition {
  programmingLanguage.availableStepDefinition(regexp_source, file_colon_line)

  private val pattern = Pattern.compile(regexp_source)

  def file_colon_line = name + "(\"" + regexp_source + "\"){ " + signature + " => ... }"

  def arguments_from(step_name: String) = JdkPatternArgumentMatcher.argumentsFrom(pattern, step_name)

  def invoke(arguments: _root_.java.util.List[Object]) {
    f(arguments.toArray.toList)
    programmingLanguage.invoked(regexp_source, file_colon_line)
  }
}