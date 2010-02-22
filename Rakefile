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
  sh %{git commit -a -m "Release #{version}"}
  sh %{git tag -a "v#{version}" -m "Release #{version}"}
  Dir.chdir('cuke4duke') do
    sh %{rake gemcutter:release}
    sh %{MAVEN_OPTS="-Xmx512m" mvn site:site site:deploy}
  end
  sh %{mvn deploy}
  sh %{git push}
  sh %{git push --tags}
end

desc 'Generate i18n Step Definitions'
task :i18n_generate do
  require 'gherkin/i18n'
  require 'erb'

  def classify(language)
    language.sanitized_key.upcase
  end

  java = ERB.new(IO.read(File.dirname(__FILE__) + '/cuke4duke/src/main/code_generator/java_annotation.erb'), nil, '-')
  Gherkin::I18n.all.each do |language|
    File.open(File.dirname(__FILE__) + '/cuke4duke/src/main/java/cuke4duke/annotation/' + classify(language) + '.java', 'wb') do |io|
      io.write(java.result(binding))
    end
  end

  scala = ERB.new(IO.read(File.dirname(__FILE__) + '/cuke4duke/src/main/code_generator/scala_trait.erb'), nil, '-')
  File.open(File.dirname(__FILE__) + '/cuke4duke/src/main/scala/cuke4duke/scala/I18n.scala', 'wb') do |io|
    io.write("package cuke4duke.scala\n\n")
    Gherkin::I18n.all.each do |language|
      io.write(scala.result(binding))
    end
  end
end