import groovy.text.SimpleTemplateEngine
import io.cucumber.gherkin.GherkinDialectProvider

import java.nio.file.Files

SimpleTemplateEngine engine = new SimpleTemplateEngine()

def unsupported = ["em"] // The generated files for Emoij do not compile.
GherkinDialectProvider dialectProvider = new GherkinDialectProvider()

dialectProvider.getLanguages().each { language ->
    def dialect = dialectProvider.getDialect(language, null)
    def normalized_language = dialect.language.replaceAll("[\\s-]", "_").toLowerCase()
    if (!unsupported.contains(normalized_language)) {
        def templateSource = new File(project.baseDir, "src/main/groovy/lambda.java.gsp").getText()
        def className = "${normalized_language}".capitalize()
        def name = dialect.name + ((dialect.name == dialect.nativeName) ? '' : ' - ' + dialect.nativeName)
        def binding = ["i18n": dialect, "className": className, "language_name": name]
        def template = engine.createTemplate(templateSource).make(binding)
        def file = new File(project.baseDir, "target/generated-sources/i18n/java/io/cucumber/java8/${className}.java")
        Files.createDirectories(file.parentFile.toPath())
        file.write(template.toString(), "UTF-8")
    }
}
