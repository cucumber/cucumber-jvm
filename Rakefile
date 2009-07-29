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
  sh %{mvn clean -P examples install}
  sh %{mvn deploy}
  Dir.chdir('cuke4duke') do
    sh %{mvn site:site site:deploy}
  end
  sh %{git commit -a -m "Release #{version}"}
  sh %{git tag -a "v#{version}" -m "Release #{version}"}
  sh %{git push}
  sh %{git push --tags}
end