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
          if(match)
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

      class HookDefinition
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

def Given(regexp, &proc)
  if block_given?
    register(regexp, proc)
  else
    call_step(regexp)
  end
end

def When(regexp, &proc)
  if block_given?
    register(regexp, proc)
  else
    call_step(regexp)
  end
end

def Then(regexp, &proc)
  if block_given?
    register(regexp, proc)
  else
    call_step(regexp)
  end
end

def Before(&proc)
  $backend.addBeforeHook(Cucumber::Runtime::JRuby::HookDefinition.new(proc))
end

def After(&proc)
  $backend.addAfterHook(Cucumber::Runtime::JRuby::HookDefinition.new(proc))
end

#def Given(string)
#  call_step string
#end
#
#def When(string)
#  call_step string
#end
#
#def Then(string)
#  call_step(string)
#end
#
def call_step(string)
  $world.getStepDefinitions().each do |step_def|
    if step_def.getPattern().match(string)
      step_def.execute Array.new
      break
    end
  end
end