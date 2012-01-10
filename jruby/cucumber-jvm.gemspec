# -*- encoding: utf-8 -*-

Gem::Specification.new do |s|
  s.name         = 'cucumber-jvm'
  s.version      = '1.0.0'
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
