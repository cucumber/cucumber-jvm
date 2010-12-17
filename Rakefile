require 'rubygems'
require 'bundler'
Bundler.setup

$:.unshift(File.dirname(__FILE__) + '/lib')
require 'cuke4duke/version'

# Make sure subprocesses use our local code.
ENV['MAVEN_OPTS'] = (ENV['MAVEN_OPTS'] || '') + " -Dcuke4duke.bin=#{File.expand_path("../bin/cuke4duke", __FILE__)}"
ENV['RUBYLIB'] = File.expand_path("../lib", __FILE__)

class ReleaseHelper < Bundler::GemHelper
  def install
    desc "Create tag #{version_tag} and upload #{name}-#{version}.jar and #{name}-#{version}.gem"
    task 'release' do
      release_jar_and_gem
    end

    desc 'Build gem'
    task 'gem' do
      build_gem
    end

    task 'maven_release' do
      maven_release
    end

    task 'remove_snapshots' do
      remove_snapshots
    end

    task 'add_snapshots' do
      add_snapshots
    end
  end

  def release_jar_and_gem
    guard_clean
    guard_already_tagged
    built_gem_path = build_gem
    tag_version {
      maven_release
      rubygem_push(built_gem_path)
      git_push
    }
  end

  def remove_snapshots
    system(%{find . -name 'pom.xml' -exec sed -i '' 's/-SNAPSHOT//' '{}' \\;})
  end

  def add_snapshots
    if Cuke4Duke::VERSION =~ /(\d+\.\d+\.)(\d+)$/
      major_minor = $1
      new_patch = $2.to_i + 1
      default_snapshot = "#{major_minor}#{new_patch}-SNAPSHOT"
      asked_snapshot = Bundler.ui.instance_variable_get('@shell').ask("What is the new SNAPSHOT version? (#{default_snapshot})", :green)
      
      snapshot = asked_snapshot.strip == "" ? default_snapshot : snapshot
      system(%{find . -name 'pom.xml' -exec sed -i '' 's/#{Cuke4Duke::VERSION}/#{snapshot}/' '{}' \\;})
    else
      raise "You're already at a -SNAPSHOT version: #{Cuke4Duke::VERSION}"
    end
  end

  def maven_release
    # We have to run this in 2 separate mvn invocations to avoid:
    #
    # Unable to configure Wagon: 'scp'
    # Embedded error: While configuring wagon for 'cukes': Unable to apply wagon configuration.
    # Cannot find setter nor field in org.apache.maven.wagon.providers.ssh.jsch.ScpWagon for 'httpHeaders'
    Dir.chdir('cuke4duke') do
     sh %{MAVEN_OPTS="-Xmx512m" mvn site:site}
     sh %{mvn site:deploy}
    end
    sh %{mvn deploy}
  end
end

ReleaseHelper.install_tasks

task :default => :build_all

task :build_all => :i18n_generate do
  Dir['lib/*.jar'].each{|jar| FileUtils.rm(jar)}
  sh('mvn -P examples clean install')
end

desc 'Generate i18n Step Definitions'
task :i18n_generate do
  require 'gherkin/i18n'
  require 'erb'

  java = ERB.new(IO.read(File.dirname(__FILE__) + '/components/core/src/main/code_generator/I18n.java.erb'), nil, '-')
  Gherkin::I18n.all.each do |i18n|
    File.open(File.dirname(__FILE__) + "/components/core/src/main/java/cucumber/annotation/#{i18n.underscored_iso_code.upcase}.java", 'wb') do |io|
      io.write(java.result(binding))
    end
  end

#  scala = ERB.new(IO.read(File.dirname(__FILE__) + '/components/core/src/main/code_generator/I18n.scala.erb'), nil, '-')
#  File.open(File.dirname(__FILE__) + '/components/core/src/main/scala/cuke4duke/I18n.scala', 'wb') do |io|
#    io.write("package cuke4duke\n\n")
#    Gherkin::I18n.all.each do |i18n|
#      io.write(scala.result(binding))
#    end
#  end
end

desc 'Make all files use UNIX (\n) line endings'
task :fix_cr_lf do
  files = FileList['**/*']
  files.each do |f|
    next if File.directory?(f)
    s = IO.read(f)
    s.gsub!(/\r?\n/, "\n")
    File.open(f, "w") { |io| io.write(s) }
  end
end
