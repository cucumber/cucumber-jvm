# encoding: utf-8
ENV['NODOT'] = 'true' # We don't want class diagrams in RDoc
$:.unshift(File.join(File.dirname(__FILE__), 'lib'))
task :gem => :jar
require 'cucumber/java/version'
require 'hoe'

AUTHOR = 'Aslak Hellesøy'  # can also be an array of Authors
EMAIL = "aslak.hellesoy@gmail.com"
DESCRIPTION = "Cucumber for Java"
GEM_NAME = 'cucumber-java' # what ppl will type to install your gem
HOMEPATH = "http://cukes.info"
RUBYFORGE_PROJECT = 'rspec'

@config_file = "~/.rubyforge/user-config.yml"
@config = nil
RUBYFORGE_USERNAME = "aslak_hellesoy"
def rubyforge_username
  unless @config
    begin
      @config = YAML.load(File.read(File.expand_path(@config_file)))
    rescue
      puts <<-EOS
ERROR: No rubyforge config file found: #{@config_file}
Run 'rubyforge setup' to prepare your env for access to Rubyforge
 - See http://newgem.rubyforge.org/rubyforge.html for more details
      EOS
      exit
    end
  end
  RUBYFORGE_USERNAME.replace @config["username"]
end

RDOC_OPTS = ['--quiet', '--title', 'Cucumber documentation',
    "--opname", "index.html",
    "--line-numbers", 
    "--main", "README.textile",
    "--inline-source"]

class Hoe
  def extra_deps 
    @extra_deps.reject! { |x| Array(x).first == 'hoe' } 
    @extra_deps
  end 
end

# Generate all the Rake tasks
# Run 'rake -T' to see list of generated tasks (from gem root directory)
$hoe = Hoe.new(GEM_NAME, Cucumber::Java::VERSION::STRING) do |p|
  p.developer(AUTHOR, EMAIL)
  p.description = DESCRIPTION
  p.summary = DESCRIPTION
  p.url = HOMEPATH
  p.rubyforge_name = RUBYFORGE_PROJECT if RUBYFORGE_PROJECT
  p.clean_globs |= ['**/.*.sw?', '*.gem', '.config', '**/.DS_Store', '**/*.class']  #An array of file patterns to delete on clean.
  
  # == Optional
  p.changes = p.paragraphs_of("History.txt", 0..1).join("\n\n")
  #p.extra_deps = []     # An array of rubygem dependencies [name, version], e.g. [ ['active_support', '>= 1.3.1'] ]
  p.extra_deps = [ 
    ['cucumber', '>= 0.2.2']
  ]

  #p.spec_extras = {}    # A hash of extra values to set in the gemspec.
  
end

CHANGES = $hoe.paragraphs_of('History.txt', 0..1).join("\\n\\n")
PATH    = (RUBYFORGE_PROJECT == GEM_NAME) ? RUBYFORGE_PROJECT : "#{RUBYFORGE_PROJECT}/#{GEM_NAME}"
$hoe.remote_rdoc_dir = File.join(PATH.gsub(/^#{RUBYFORGE_PROJECT}\/?/,''), 'rdoc')
$hoe.rsync_args = '-av --delete --ignore-errors'

# Hoe gives us :default => :test, but we don't have Test::Unit tests.
Rake::Task[:default].clear_prerequisites rescue nil # For some super weird reason this fails for some...

task :jar do
  sh 'mvn clean package'
  mv "target/cucumber-java-#{Cucumber::Java::VERSION::STRING}.jar", 'lib'
  sh 'mvn clean'
end
