require 'rubygems'
require 'cucumber'
require 'cuke4duke/version'
begin
  require "cuke4duke-#{Cuke4Duke::VERSION}.jar"
rescue LoadError
  require "cuke4duke-#{Cuke4Duke::VERSION.gsub(/\.beta$/, '-SNAPSHOT')}.jar"
end
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
