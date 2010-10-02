# -*- encoding: utf-8 -*-
require 'rexml/document'

Gem::Specification.new do |s|
  s.name        = 'cuke4duke'
  s.version     = REXML::XPath.first(REXML::Document.new(IO.read(File.dirname(__FILE__) + '/pom.xml')), '//xmlns:project/xmlns:version/text()')
  s.authors     = ["Aslak Hellesøy"]
  s.description = 'Write Cucumber Step Definitions in Java, Scala, Groovy, Rhino Javascript, Clojure or Ioke'
  s.summary     = "cuke4duke-#{s.version}"
  s.email       = 'cukes@googlegroups.com'
  s.homepage    = 'http://cukes.info'

  s.add_dependency 'cucumber', '~> 0.9.1'

  s.rubygems_version   = "1.3.7"
  s.files            = `git ls-files -- lib`.split("\n")
  s.files            << "lib/cuke4duke-#{s.version}.jar"
  s.files            << "pom.xml"
  s.executables      = `git ls-files -- bin/*`.split("\n").map{ |f| File.basename(f) }
  s.extra_rdoc_files = ["LICENCE", "README.textile"]
  s.rdoc_options     = ["--charset=UTF-8"]
  s.require_path     = "lib"
end
