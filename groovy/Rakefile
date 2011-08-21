require 'gherkin/i18n'
require 'erb'

task :default => :generate

desc 'Generate Groovy code'
task :generate do
  puts "== Generating Groovy"
  groovy = ERB.new(IO.read(File.dirname(__FILE__) + '/src/main/code_generator/I18n.groovy.erb'), nil, '-')
  Gherkin::I18n.all.each do |i18n|
    file = File.dirname(__FILE__) + "/src/main/java/cucumber/runtime/groovy/#{i18n.underscored_iso_code.upcase}.java"
    puts "* #{File.expand_path(file)}"
    File.open(file, 'wb') do |io|
      io.write(groovy.result(binding))
    end
  end
end
