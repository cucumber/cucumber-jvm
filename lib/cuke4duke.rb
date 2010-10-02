require 'rubygems'
require 'cucumber'

module Cuke4Duke
  VERSION = Gem::Specification.load(File.dirname(__FILE__) + '/../cuke4duke.gemspec').version
end
require "cuke4duke-#{Cuke4Duke::VERSION}.jar"
Cucumber::VERSION << " (cuke4duke #{Cuke4Duke::VERSION})"

require 'cucumber/formatter/unicode'

# Workaround to make the java code have access to the same Ruby
# interpreter as the one that is used to run this script.
# org.jruby.Main uses a different instance than what 
# org.jruby.Ruby.getGlobalRuntime() returns. It might be considered
# a JRuby bug.
Java.cuke4duke.spi.jruby.JRuby.setRuntime(JRuby.runtime)

require 'cuke4duke/step_mother_ext'
require 'cuke4duke/py_string_ext'
require 'cuke4duke/table_ext'
require 'cuke4duke/scenario_ext'
require 'cuke4duke/step_match_ext'
