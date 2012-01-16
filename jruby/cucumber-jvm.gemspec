# -*- encoding: utf-8 -*-

version = IO.read(File.dirname(__FILE__) + '/../build.properties').match(/^\s*cucumber-jvm\.version\s*=\s*(.*)$/n)[1].strip

Gem::Specification.new do |s|
  s.name         = 'cucumber-jvm'
  s.version      = "#{version}"
  s.authors      = ['Aslak Hellesøy']
  s.description  = 'Cucumber-JVM for JRuby'
  s.summary      = "#{s.name}-#{s.version}"
  s.email        = 'cukes@googlegroups.com'
  s.homepage     = 'http://github.com/cucumber/cucumber-jvm'
  s.files        = Dir['lib/**/*'] + Dir['bin/**/*']
  s.files       -= Dir['**/.gitignore']
  s.executables  = ['cucumber-jvm']
  s.require_path = 'lib'
  s.platform     = 'java'
end
