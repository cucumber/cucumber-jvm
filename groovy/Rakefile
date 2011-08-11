require 'rubygems'
require 'gherkin/i18n'
require 'erb'

task :default => ['generate:groovy']

namespace :generate do
  desc 'Generate Groovy code'
  task :groovy do
    groovy = ERB.new(IO.read(File.dirname(__FILE__) + '/src/main/code_generator/I18n.groovy.erb'), nil, '-')
    Gherkin::I18n.all.each do |i18n|
      File.open(File.dirname(__FILE__) + "/src/main/java/cucumber/runtime/groovy/#{i18n.underscored_iso_code.upcase}.java", 'wb') do |io|
        io.write(groovy.result(binding))
      end
    end
  end
end
