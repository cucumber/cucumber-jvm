import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/* This class generates the cucumber-java Interfaces and package-info
 * based on the languages and keywords from the GherkinDialectProvider
 * using the FreeMarker template engine and provided templates.
 */
public class CodeGenerationJava {

    public static void main(String[] args) throws Exception {

        // 1. Configure template engine
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
        cfg.setClassForTemplateLoading(CodeGenerationJava.class, "templates");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // 2. Add templates
        Template templateSource = cfg.getTemplate("annotation.java.ftl");
        Template packageInfoSource = cfg.getTemplate("package-info.ftl");

        // 3. Process template input and generate files
        List<String> unsupported = new ArrayList<>();
        unsupported.add("em");
        unsupported.add("en_tx"); // The generated files for Emoij and Texan do
        // not compile.

        // Get languages and keywords from GherkinDialectProvider
        GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        dialectProvider.getLanguages().forEach(
            language -> {
                GherkinDialect dialect = dialectProvider.getDialect(language).get();
                String normalizedLanguage = dialect.getLanguage().replaceAll("[\\s-]", "_").toLowerCase();
                if (!unsupported.contains(normalizedLanguage)) {
                    dialect.getStepKeywords().stream()
                            .filter(it -> !it.contains(String.valueOf('*')))
                            .filter(it -> !it.matches("^\\d.*")).distinct()
                            .forEach(keyword -> {
                                String normalizedKeyword = normalize(keyword.replaceAll("[\\s',!\u00AD]", ""));
                                LinkedHashMap<String, String> binding = new LinkedHashMap<>();
                                binding.put("lang", normalizedLanguage);
                                binding.put("kw", normalizedKeyword);
                                try {
                                    createInterfaces(templateSource, normalizedLanguage, normalizedKeyword, binding);
                                } catch (IOException | TemplateException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                    // Add package-info.java
                    String name = dialect.getName() + ((dialect.getName().equals(dialect.getNativeName())) ? ""
                            : " - " + dialect.getNativeName());
                    LinkedHashMap<String, String> binding = new LinkedHashMap<>();
                    binding.put("normalized_language", normalizedLanguage);
                    binding.put("language_name", name);
                    try {
                        createPackageInfo(packageInfoSource, normalizedLanguage, binding);
                    } catch (IOException | TemplateException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
    }

    private static void createInterfaces(
            Template templateSource, String normalizedLanguage, String normalizedKeyword,
            LinkedHashMap<String, String> binding
    ) throws IOException, TemplateException {
        String fileName = "target/generated-sources/i18n/java/io/cucumber/java/" + normalizedLanguage
                + "/" + normalizedKeyword + ".java";
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            // Haitian has two translations that only differ by case - Sipozeke
            // and SipozeKe
            // Some file systems are unable to distinguish between them and
            // overwrite the other one :-(
            Files.createDirectories(path.getParent());
            BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE);
            templateSource.process(binding, writer);
        }
    }

    private static void createPackageInfo(
            Template packageInfoSource, String normalizedLanguage, LinkedHashMap<String, String> binding
    ) throws IOException, TemplateException {
        String fileName = "target/generated-sources/i18n/java/io/cucumber/java/" + normalizedLanguage
                + "/package-info.java";
        Path path = Paths.get(fileName);
        Files.createDirectories(path.getParent());
        BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE);
        packageInfoSource.process(binding, writer);
    }

    static String normalize(CharSequence s) {
        return Normalizer.normalize(s, Normalizer.Form.NFC);
    }
}
