module Cucumber
  module Runtime
    module JRuby
      class StepDefinition
        PROC_PATTERN = /[\d\w]+@(.+):(\d+).*>/
        PWD = Dir.pwd

        def initialize(regexp, proc)
          @regexp, @proc = regexp, proc
        end

        # Lifted from regexp_argument_matcher.rb in Cucumber 1.0
        def matched_arguments(step_name)
          match = @regexp.match(step_name)
          if match
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

        def arg_count
          @proc.arity
        end

        def execute(*args)
          $world.instance_exec(*args, &@proc)
        end

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

        def pattern
          @regexp.inspect
        end
      end
    end
  end
end

def register(regexp, proc)
  $backend.registerStepdef(Cucumber::Runtime::JRuby::StepDefinition.new(regexp, proc))
end

def Given(regexp, &proc)
  register(regexp, proc)
end

def When(regexp, &proc)
  register(regexp, proc)
end

def Then(regexp, &proc)
  register(regexp, proc)
end