import groovy.text.SimpleTemplateEngine
import io.cucumber.gherkin.GherkinDialectProvider

import java.nio.file.Files
import java.text.Normalizer

SimpleTemplateEngine engine = new SimpleTemplateEngine()
def templateSource = new File(project.baseDir, "src/main/groovy/annotation.java.gsp").getText()
def packageInfoSource = new File(project.baseDir, "src/main/groovy/package-info.java.gsp").getText()

static def normalize(s) {
    return Normalizer.normalize(s, Normalizer.Form.NFC)
}

def unsupported = ["em"] // The generated files for Emoij do not compile.
GherkinDialectProvider dialectProvider = new GherkinDialectProvider()

dialectProvider.getLanguages().each { language ->
    def dialect = dialectProvider.getDialect(language, null)
    def normalized_language = dialect.language.replaceAll("[\\s-]", "_").toLowerCase()
    if (!unsupported.contains(normalized_language)) {
        dialect.stepKeywords.findAll { !it.contains('*') && !it.matches("^\\d.*") }.unique().each { kw ->
            def normalized_kw = normalize(kw.replaceAll("[\\s',!\u00AD]", ""))
            def binding = ["lang": normalized_language, "kw": normalized_kw]
            def template = engine.createTemplate(templateSource).make(binding)
            def file = new File(project.baseDir, "target/generated-sources/i18n/java/io/cucumber/java/${normalized_language}/${normalized_kw}.java")
            if (!file.exists()) {
                // Haitian has two translations that only differ by case - Sipozeke and SipozeKe
                // Some file systems are unable to distiguish between them and overwrite the other one :-(
                Files.createDirectories(file.parentFile.toPath())
                file.write(template.toString(), "UTF-8")
            }
        }

        // package-info.java
        def name = dialect.name + ((dialect.name == dialect.nativeName) ? '' : ' - ' + dialect.nativeName)
        def binding = ["normalized_language": normalized_language, "language_name": name]
        def html = engine.createTemplate(packageInfoSource).make(binding).toString()
        def file = new File(project.baseDir, "target/generated-sources/i18n/java/io/cucumber/java/${normalized_language}/package-info.java")
        file.write(html, "UTF-8")
    }
}
