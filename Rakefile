require 'cucumber/rake/task'

desc "Release"
task :release => [:git_clean, :generate] do
  sh "mvn release:clean"
  sh "mvn --batch-mode -P release-sign-artifacts release:prepare"
  sh "mvn --batch-mode -P release-sign-artifacts release:perform"
end

task :git_clean do
  sh "git clean -df"
end

desc "Generate code"
task :generate do
  Dir['*/Rakefile'].each do |rakefile|
    Dir.chdir(File.dirname(rakefile)) do
      if ENV['SKIP_BUNDLER'].to_s == 'true'
        puts `rake generate`
      else
        require 'bundler/setup'
        puts `bundle exec rake generate`
      end
    end
  end
end

Cucumber::Rake::Task.new(:picocontainer) do |t|
  t.cucumber_opts = '-r java/src/test/resources/cucumber-features -r cucumber-features cucumber-features'
end

task :default => :picocontainer