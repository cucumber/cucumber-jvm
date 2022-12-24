package codegen;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class CodeGenerationJava8 {

    public static void main(String[] args) throws IOException {

        // 1. Configure template engine
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setClassForTemplateLoading(CodeGenerationJava8.class, "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // 2. Add template
        Template templateSource = cfg.getTemplate("lambda.java.ftl");

        // 3. Process template input and generate files
        List<String> unsupported = new ArrayList<>();
        unsupported.add("em");
        unsupported.add("en_tx"); // The generated files for Emoij and Texan do
        // not compile.

        GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        dialectProvider.getLanguages().forEach(
            language -> {
                GherkinDialect dialect = dialectProvider.getDialect(language).get(); // TODO:
                // Optional.isPresent()
                // check
                // (?)
                String normalizedLanguage = dialect.getLanguage().replaceAll("[\\s-]", "_").toLowerCase();
                if (!unsupported.contains(normalizedLanguage)) {
                    String className = capitalize(normalizedLanguage);
                    String name = dialect.getName() + ((dialect.getName().equals(dialect.getNativeName())) ? ""
                            : " - " + dialect.getNativeName());
                    LinkedHashMap<String, Object> binding = new LinkedHashMap<>();
                    binding.put("language_name", name);
                    binding.put("className", className);
                    // binding.put("kw", keyword(dialect));
                    binding.put("keywords", extractKeywords(dialect));
                    try {
                        createInterfaces(templateSource, className, binding);
                    } catch (IOException | TemplateException e) {
                        throw new RuntimeException(e);
                    }
                }

            });
    }

    // Extract sorted keywords from the dialect, and normalize them
    private static List extractKeywords(GherkinDialect dialect) {
        List<String> keywords = new ArrayList<>();
        dialect.getStepKeywords().stream().sorted()
                .filter(it -> !it.contains(String.valueOf('*')))
                .filter(it -> !it.matches("^\\d.*")).distinct()
                .forEach(keyword -> {
                    String normalizedKeyword = normalize(keyword.replaceAll("[\\s',!\u00AD]", ""));
                    keywords.add(normalizedKeyword);
                });
        return keywords;
    }

    private static void createInterfaces(
            Template templateSource, String className, LinkedHashMap<String, Object> binding
    ) throws IOException, TemplateException {
        String fileName = "target/generated-sources/i18n/java/io/cucumber/java8/" + className + ".java";
        File basedir = new File(Paths.get("cucumber-java8").toAbsolutePath().toString());
        File file = new File(basedir, fileName);
        Files.createDirectories(file.getParentFile().toPath());
        Writer fileWriter = new FileWriter(file);
        templateSource.process(binding, fileWriter);
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    static String normalize(CharSequence s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }
}
