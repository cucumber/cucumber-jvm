# This file is loaded before Cucumber is loaded.
require 'rubygems'
gem 'cucumber', '>= 0.4.4'
require 'java'

# Workaround to make the java code have access to the same Ruby
# interpreter as the one that is used to run this script.
# org.jruby.Main uses a different instance than what 
# org.jruby.Ruby.getGlobalRuntime() returns. It might be considered
# a JRuby bug.
Java.cuke4duke.internal.JRuby.setRuntime(JRuby.runtime)

require 'cuke4duke/step_mother_ext'
require 'cuke4duke/py_string_ext'
require 'cuke4duke/table_ext'
