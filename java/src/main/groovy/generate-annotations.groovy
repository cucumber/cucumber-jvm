import gherkin.GherkinDialectProvider
import groovy.text.SimpleTemplateEngine

import java.text.Normalizer

SimpleTemplateEngine engine = new SimpleTemplateEngine()
def templateSource = new File(project.baseDir, "src/main/groovy/annotation.java.gsp").getText()
def deprecatedTemplateSource = new File(project.baseDir, "src/main/groovy/deprecated-annotation.java.gsp").getText()

static def normalize(s) {
    if (System.getProperty("java.version").startsWith("1.6")) {
        return s
    } else {
        return Normalizer.normalize(s, Normalizer.Form.NFC)
    }
}

def localeFor(lang) {
    languageAndCountry = lang.split("-")
    if (languageAndCountry.length == 1) {
        return new Locale(lang)
    } else {
        return new Locale(languageAndCountry[0], languageAndCountry[1])
    }
}

// TODO: Need to add i18n.getName() and i18n.getNative() for better names.
def package_info_java = """\
/**
 * \${locale.getDisplayLanguage()}
 */
package io.cucumber.java.api.\${normalized_language}; 
"""

def deprecated_package_info_java = """\
/**
 * \${locale.getDisplayLanguage()}
 */
package cucumber.api.java.\${normalized_language}; 
"""


def unsupported = ["em"] // The generated files for Emoij do not compile.
def dialectProvider = new GherkinDialectProvider()

GherkinDialectProvider.DIALECTS.keySet().each { language ->
    def dialect = dialectProvider.getDialect(language, null)
    def normalized_language = dialect.language.replaceAll("[\\s-]", "_").toLowerCase()
    if (!unsupported.contains(normalized_language)) {
        dialect.stepKeywords.findAll { !it.contains('*') && !it.matches("^\\d.*") }.unique().each { kw ->
            def normalized_kw = normalize(kw.replaceAll("[\\s',!\u00AD]", ""))
            def binding = ["lang": normalized_language, "kw": normalized_kw]
            def template = engine.createTemplate(templateSource).make(binding)
            def file = new File(project.baseDir, "target/generated-sources/i18n/java/io/cucumber/java/api/${normalized_language}/${normalized_kw}.java")
            if (!file.exists()) {
                // Haitian has two translations that only differ by case - Sipozeke and SipozeKe
                // Some file systems are unable to distinguish between them and overwrite the other one :-(
                file.parentFile.mkdirs()
                file.write(template.toString(), "UTF-8")
            }

            def deprecatedTemplate = engine.createTemplate(deprecatedTemplateSource).make(binding)
            def deprecatedFile = new File(project.baseDir, "target/generated-sources/i18n/java/cucumber/api/java/${normalized_language}/${normalized_kw}.java")
            if (!deprecatedFile.exists()) {
                // Haitian has two translations that only differ by case - Sipozeke and SipozeKe
                // Some file systems are unable to distinguish between them and overwrite the other one :-(
                deprecatedFile.parentFile.mkdirs()
                deprecatedFile.write(deprecatedTemplate.toString(), "UTF-8")
            }
        }

        // package-info.java
        def locale = localeFor(dialect.language)
        def binding = [ "locale": locale, "normalized_language": normalized_language ]

        def html = engine.createTemplate(package_info_java).make(binding).toString()
        def file = new File(project.baseDir, "target/generated-sources/i18n/java/io/cucumber/java/api/${normalized_language}/package-info.java")
        file.write(html, "UTF-8")

        def deprecatedHtml = engine.createTemplate(deprecated_package_info_java).make(binding).toString()
        def deprecatedFile = new File(project.baseDir, "target/generated-sources/i18n/java/cucumber/api/java/${normalized_language}/package-info.java")
        deprecatedFile.write(deprecatedHtml, "UTF-8")
    }
}