require 'gherkin/i18n'
require 'erb'

task :default => :generate

desc 'Generate Scala code'
task :generate do
  puts "== Generating Scala"
  groovy = ERB.new(IO.read(File.dirname(__FILE__) + '/src/main/code_generator/I18n.scala.erb'), nil, '-')
  file = File.dirname(__FILE__) + "/src/main/scala/cucumber/runtime/I18n.scala"
  puts "* #{File.expand_path(file)}"
  File.open(file, 'wb') do |io|
    io.write(groovy.result(binding))
  end
end
