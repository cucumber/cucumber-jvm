begin
  class ::Java::Cuke4dukeInternalScala::ScalaAnalyzer
    def snippet_generator
      require 'cucumber/scala_support/scala_snippet_generator'
      Cucumber::JavaSupport::ScalaSnippetGenerator.new
    end

    Cucumber::ClassSupport::ClassLanguage.add_analyzers(self.new)
  end
rescue NameError => ignore
end