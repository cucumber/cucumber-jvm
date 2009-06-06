desc 'Make all files use UNIX (\n) line endings'
task :fix_cr_lf do
  files = FileList['**/*']
  files.each do |f|
    s = IO.read(f)
    s.gsub!(/\r?\n/, "\n")
    File.open(f, "w") { |io| io.write(s) }
  end
end