require 'java'

# Avoid warnings
# https://github.com/jruby/jruby/wiki/Persistence
Java::CucumberRuntimeJRuby::World.__persistent__ = true
Java::CucumberRuntime::ScenarioImpl.__persistent__ = true

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

      class WorldRunner
        include Locatable

        def initialize(modules_or_proc)
          @modules_or_proc = modules_or_proc
        end

        def execute(world, *args)
          @modules_or_proc.each do |module_or_proc|
            if Proc === module_or_proc
              world = world.instance_exec(*args, &module_or_proc)
            else
              world.extend(module_or_proc)
            end
          end
          world
        end
      end

      class HookRunner
        include Locatable

        def initialize(proc)
          @proc = proc
        end

        def execute(world, scenario)
          world = world.instance_exec(scenario, &@proc)
        end
      end

      class StepDefinitionRunner
        include Locatable

        def initialize(regexp, proc)
          @regexp, @proc = regexp, proc
        end

        def execute(world, *args)
          world.instance_exec(*args, &@proc)
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

        def pattern
          @regexp.inspect
        end
      end

      module Dsl
        def Before(&proc)
          $backend.registerBeforeHook(HookRunner.new(proc))
        end

        def After(&proc)
          $backend.registerAfterHook(HookRunner.new(proc))
        end

        def World(*modules_or_proc)
          # We can reuse the HookDefinition, because it quacks the same
          $backend.registerWorldBlock(WorldRunner.new(modules_or_proc))
        end

        def register_stepdef(regexp, proc)
          $backend.registerStepdef(StepDefinitionRunner.new(regexp, proc))
        end

        # TODO: The code below should be generated, just like I18n for other backends

        def Given(regexp, &proc)
          register_stepdef(regexp, proc)
        end

        def When(regexp, &proc)
          register_stepdef(regexp, proc)
        end

        def Then(regexp, &proc)
          register_stepdef(regexp, proc)
        end
      end

      module World
        def pending(reason = "TODO")
          $backend.pending(reason)
        end

        def step(name, arg=nil) # TODO: pass in an entire gherkin text instead of a step
          # caller[0] gets us to our stepdef, right before we enter the dsl
          uri, line = *caller[0].to_s.split(/:/)
          # determine if we got an argument we should pass through to calling things
          data_table = nil
          doc_string = nil
          if arg
            if arg.kind_of? Java::cucumber.api.DataTable
              data_table = arg
            else
              doc_string = arg
            end
          end

          $backend.runStep(uri, @__gherkin_i18n, 'When ', name, line.to_i, data_table, doc_string)
        end
      end

    end
  end
end

self.extend(Cucumber::Runtime::JRuby::Dsl)
