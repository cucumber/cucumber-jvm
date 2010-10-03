require 'rexml/document'

module Cuke4Duke
  VERSION = REXML::XPath.first(REXML::Document.new(IO.read(File.dirname(__FILE__) + '/../../pom.xml')), '//xmlns:project/xmlns:version/text()').to_s.gsub(/-SNAPSHOT$/, '.beta')
end
