require 'cucumber/rake/task'

Cucumber::Rake::Task.new(:tck_tests) do |t|
  t.cucumber_opts = '-r java/src/test/resources/cucumber-tck -r cucumber-tck cucumber-tck'
end

task :default => :tck_tests