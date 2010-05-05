# This file is loaded before Cucumber is loaded.
require 'rubygems'
begin
  require 'cucumber'
rescue LoadError => e
  gem 'cucumber'
  puts 'LOADPATH'
  puts $:
  puts e.message
  puts e.backtrace
  require 'cucumber'
end
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
