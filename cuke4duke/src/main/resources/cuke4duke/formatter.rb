module Cuke4duke
  # Formatter that forwards all leaf invocations to a Java cuke4duke.Visitor instance
  class Formatter < Cucumber::Ast::Visitor
    def initialize(step_mother, io, options)
      @step_mother = step_mother
    end

    def visit_features(features)
      $cuke4duke_visitor_delegate.visitFeatures
      super
    end

    def visit_scenario_name(keyword, name, file_colon_line, source_indent)
      $cuke4duke_visitor_delegate.visitScenarioName(keyword, name)
      super
    end

    def visit_step_result(keyword, step_match, multiline_arg, status, exception, source_indent, background)
      exception = Exception.new("dooders");
      # TODO: change signature:
      # $cuke4duke_visitor_delegate.visitStepResult(keyword, step_match.name, status, exception)
      $cuke4duke_visitor_delegate.visitStepResult(keyword, status.to_s, exception)
      super
    end
  end
end
