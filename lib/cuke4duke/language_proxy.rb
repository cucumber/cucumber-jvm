require 'forwardable'

module Cuke4Duke
  class << self    
    # Defines a Ruby class for +lang+ that will delegate to cuke4duke.
    def cuke4!(lang)
      require "cucumber/#{lang}_support/backtrace_filter"

      Cucumber.module_eval do
        const_set("#{lang.capitalize}Support", Module.new do
          const_set("#{lang.capitalize}Language", Class.new do
            extend Forwardable
            include Cucumber::LanguageSupport::LanguageMethods

            def_delegators :@delegate, :load_code_file, :step_matches, :begin_scenario, :end_scenario

            define_method(:initialize) do |step_mother|
              @delegate = eval("Java.cuke4duke.internal.#{lang}.#{lang.capitalize}Language", binding, __FILE__, __LINE__).new(self, Java.cuke4duke.spi.jruby.JRubyExceptionFactory.new)
            end

            def alias_adverbs(adverbs)
            end

            define_method(:snippet_text) do |step_keyword, step_name, multiline_arg_class|
              "YAY A #{lang} SNIPPET: #{step_keyword}, #{step_name}, #{multiline_arg_class}"
            end
          end)
        end)
      end
    end
  end
end