# -*- encoding: utf-8 -*-

require 'java'
version = Java::JavaLang::System.getProperty('cucumber-jvm.version')
if(version =~ /(.*)-SNAPSHOT$/)
  version=$1
end

Gem::Specification.new do |s|
  s.name         = 'cucumber-jvm'
  s.version      = "#{version}"
  s.authors      = ['Aslak Hellesøy']
  s.description  = 'Cucumber-JVM for JRuby'
  s.summary      = "#{s.name}-#{s.version}"
  s.email        = 'cukes@googlegroups.com'
  s.homepage     = 'http://github.com/cucumber/cucumber-jvm'
  s.files        = ['bin/cucumber-jvm', 'lib/cucumber-jruby.jar']
  s.executables  = ['cucumber-jvm']
  s.require_path = 'lib'
  s.platform     = 'java'
end
