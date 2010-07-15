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

desc 'Release'
task :release do
  version = IO.read('pom.xml').match(/<version>(.*)<\/version>/)[1]
  Dir.chdir('cuke4duke') do
    sh %{rake gemspec}
  end
  Dir.chdir('cuke4duke') do
    sh %{rake gemcutter:release}
    sh %{MAVEN_OPTS="-Xmx512m -Dmaven.wagon.provider.http=httpclient" mvn site:site site:deploy}
  end
  sh %{mvn -Dmaven.wagon.provider.http=httpclient deploy}
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