require 'java'

module Cucumber
  module Runtime
    module JRuby
      module Locatable
        PROC_PATTERN = /[\d\w]+@(.+):(\d+).*>/
        PWD = Dir.pwd

        # Lifted from proc.rb in Cucumber 1.0
        def file_and_line
          path, line = *@proc.inspect.match(PROC_PATTERN)[1..2]
          path = File.expand_path(path)
          pwd = File.expand_path(PWD)
          if path.index(pwd)
            path = path[pwd.length+1..-1]
          elsif path =~ /.*\/gems\/(.*\.rb)$/
            path = $1
          end
          [path, line.to_i]
        end
      end

      class StepDefinition
        include Locatable

        def initialize(regexp, proc)
          @regexp, @proc = regexp, proc
        end

        # Lifted from regexp_argument_matcher.rb in Cucumber 1.0
        def matched_arguments(step_name)
          match = @regexp.match(step_name)
          if (match)
            n = 0
            match.captures.map do |val|
              n += 1
              start = match.offset(n)[0]
              Java::GherkinFormatter::Argument.new(start, val)
            end
          else
            nil
          end
        end

        def param_count
          @proc.arity
        end

        def execute(i18n, *args)
          $world.instance_variable_set :@__gherkin_i18n, i18n
          $world.instance_exec(*args, &@proc)
        end

        def pattern
          @regexp.inspect
        end
      end

      class HookDefinition
        include Locatable

        def initialize(proc)
          @proc = proc
        end

        def execute(*args)
          $world.instance_exec(*args, &@proc)
        end
      end

    end
  end
end

def register(regexp, proc)
  $backend.addStepdef(Cucumber::Runtime::JRuby::StepDefinition.new(regexp, proc))
end

def register_or_invoke(keyword, regexp_or_name, arg, proc)
  if proc
    register(regexp_or_name, proc)
  else
    # caller[1] gets us to our stepdef, right before we enter the dsl
    uri, line = *caller[1].to_s.split(/:/)
    # determine if we got an argument we should pass through to calling things
    data_table = nil
    doc_string = nil
    if arg
      if arg.kind_of? Java::cucumber.table.DataTable
        data_table = arg
      elsif arg.kind_of? Java::gherkin.formatter.model.DocString
        doc_string = arg
      end
    end

    $backend.runStep(uri, @__gherkin_i18n, keyword, regexp_or_name, line.to_i, data_table, doc_string)
  end
end

def pending(reason = "TODO")
  $backend.pending(reason)
end

def Before(&proc)
  $backend.addBeforeHook(Cucumber::Runtime::JRuby::HookDefinition.new(proc))
end

def After(&proc)
  $backend.addAfterHook(Cucumber::Runtime::JRuby::HookDefinition.new(proc))
end

def World(&proc)
  # I can reuse the HookDefinition, because it quacks the same
  $backend.addWorldBlock(Cucumber::Runtime::JRuby::HookDefinition.new(proc))
end

# TODO: The code below should be generated, just like I18n for other backends

def Given(regexp_or_name, arg = nil, &proc)
  register_or_invoke('Given ', regexp_or_name, arg, proc)
end

def When(regexp_or_name, arg = nil, &proc)
  register_or_invoke('When ', regexp_or_name, arg, proc)
end

def Then(regexp_or_name, arg = nil, &proc)
  register_or_invoke('Then ', regexp_or_name, arg, proc)
end
