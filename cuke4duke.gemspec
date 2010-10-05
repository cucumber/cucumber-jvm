# -*- encoding: utf-8 -*-
$LOAD_PATH.unshift File.expand_path("../lib", __FILE__)
require 'cuke4duke/version'

Gem::Specification.new do |s|
  s.name        = 'cuke4duke'
  s.version     = Cuke4Duke::VERSION
  s.authors     = ["Aslak Hellesøy"]
  s.description = 'Write Cucumber Step Definitions in Java, Scala, Groovy, Rhino Javascript, Clojure or Ioke'
  s.summary     = "cuke4duke-#{s.version}"
  s.email       = 'cukes@googlegroups.com'
  s.homepage    = 'http://cukes.info'

  s.add_dependency 'cucumber', '~> 0.9.1'
  s.add_development_dependency 'rspec', '>= 2.0.0.beta.22'
  s.add_development_dependency 'celerity', '>= 0.8.2'
  s.add_development_dependency 'jruby-openssl', '>= 0.7.1'
  s.add_development_dependency 'bundler', '>= 1.0.2'

  s.rubygems_version   = "1.3.7"
  s.files            = `git ls-files -- lib`.split("\n") + ["lib/#{Cuke4Duke::JAR_NAME}", 'pom.xml']
  s.executables      = `git ls-files -- bin/*`.split("\n").map{ |f| File.basename(f) }
  s.extra_rdoc_files = ["LICENCE", "README.textile"]
  s.rdoc_options     = ["--charset=UTF-8"]
  s.require_path     = "lib"
end
