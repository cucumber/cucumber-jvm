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

    task 'maven_release' do
      maven_release
    end
  end

  def release_jar_and_gem
    guard_clean
    guard_already_tagged
    built_gem_path = build_gem
    tag_version {
      git_push
      maven_release
      rubygem_push(built_gem_path)
    }
  end

  def remove_snapshots
    system(%{find . -name 'pom.xml' -exec sed -i '' 's/-SNAPSHOT//' '{}' \;})
  end

  def add_snapshots
    unless Cuke4Duke::VERSION =~ /(\d+\.\d+\.)(\d+)-SNAPSHOT$/
      new_major = $2.to_i + 1
      new_snapshot = "#{$1}#{new_major}-SNAPSHOT"
      puts new_snapshot
      #system(%{find . -name 'pom.xml' -exec sed -i '' 's/-SNAPSHOT//' '{}' \;})
    else
      raise "You're already at a -SNAPSHOT version: #{Cuke4Duke::VERSION}"
    end
  end

  def maven_release
  end
end

ReleaseHelper.install_tasks

task :build_all => :i18n_generate do
  Dir['lib/*.jar'].each{|jar| FileUtils.rm(jar)}
  sh('mvn -P examples clean install')
end

desc 'Release'
task :releaseXX do
  version = IO.read('pom.xml').match(/<version>(.*)<\/version>/)[1]

  # Disabled because of this retarded Maven error:
  #
  # Unable to configure Wagon: 'scp'
  # Embedded error: While configuring wagon for 'cukes': Unable to apply wagon configuration.
  # Cannot find setter nor field in org.apache.maven.wagon.providers.ssh.jsch.ScpWagon for 'httpHeaders'
  # Dir.chdir('cuke4duke') do
  #  sh %{MAVEN_OPTS="-Xmx512m" mvn site:site site:deploy}
  # end
  
  #sh %{mvn -Dmaven.wagon.provider.http=httpclient deploy}
  sh %{mvn deploy}
  sh %{git push}
  sh %{git tag -a "v#{version}" -m "Release #{version}"}
  sh %{git push --tags}
end

desc 'Generate i18n Step Definitions'
task :i18n_generate do
  require 'gherkin/i18n'
  require 'erb'

  java = ERB.new(IO.read(File.dirname(__FILE__) + '/cuke4duke/src/main/code_generator/I18n.java.erb'), nil, '-')
  File.open(File.dirname(__FILE__) + '/cuke4duke/src/main/java/cuke4duke/annotation/I18n.java', 'wb') do |io|
    io.write(<<-HEADER)
package cuke4duke.annotation;

import cuke4duke.internal.java.annotation.StepDef;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public interface I18n {

    HEADER
    Gherkin::I18n.all.each do |i18n|
      io.write(java.result(binding))
    end

    io.write("}")
  end

  scala = ERB.new(IO.read(File.dirname(__FILE__) + '/cuke4duke/src/main/code_generator/I18n.scala.erb'), nil, '-')
  File.open(File.dirname(__FILE__) + '/cuke4duke/src/main/scala/cuke4duke/I18n.scala', 'wb') do |io|
    io.write("package cuke4duke\n\n")
    Gherkin::I18n.all.each do |i18n|
      io.write(scala.result(binding))
    end
  end
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
