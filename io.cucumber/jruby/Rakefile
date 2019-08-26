# Rake task for generating API docs
require 'nokogiri'
require 'yard'
require 'yard/rake/yardoc_task'

CUCUMBER_JVM_VERSION = Nokogiri::XML(IO.read('../pom.xml')).xpath('//xmlns:project/xmlns:version').text
YARD::Templates::Engine.register_template_path('yard')
YARD::Rake::YardocTask.new(:yard) do |yard|
  yard.files = FileList['src/main/resources/**/*'] + FileList['target/generated-resources/i18n/**/*']
end

task :default => :yard