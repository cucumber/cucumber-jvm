require 'bundler/setup'
require 'cucumber/rake/task'

task :generate do
  Dir['*/Rakefile'].each do |rakefile|
    Dir.chdir(File.dirname(rakefile)) do
      sh "bundle exec rake generate"
    end
  end
end

Cucumber::Rake::Task.new(:picocontainer) do |t|
  t.cucumber_opts = '-r java/src/test/resources/cucumber-features -r cucumber-features cucumber-features'
end

task :default => :picocontainer