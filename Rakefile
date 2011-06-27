require 'cucumber/rake/task'

Cucumber::Rake::Task.new(:picocontainer) do |t|
  t.cucumber_opts = '-r java/src/test/resources/cucumber-features -r cucumber-features cucumber-features'
end

task :default => :picocontainer