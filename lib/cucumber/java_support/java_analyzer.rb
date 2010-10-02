begin
  class ::Java::Cuke4dukeInternalJava::JavaAnalyzer
    def snippet_generator
      require 'cucumber/java_support/java_snippet_generator'
      Cucumber::JavaSupport::JavaSnippetGenerator.new
    end
    
    Cucumber::ClassSupport::ClassLanguage.add_analyzers(self.new)
  end
rescue NameError => ignore
end