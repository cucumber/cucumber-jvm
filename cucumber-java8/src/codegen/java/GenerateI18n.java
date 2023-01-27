import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;

/* This class generates the cucumber-java Interfaces and package-info
 * based on the languages and keywords from the GherkinDialectProvider
 * using the FreeMarker template engine and provided templates.
 */
public class GenerateI18n {

    // The generated files for and Emoij do not compile :(
    private static final List<String> unsupported = Arrays.asList("em", "en-tx");

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: <baseDirectory> <packagePath>");
        }

        DialectWriter dialectWriter = new DialectWriter(args[0], args[1]);

        // Generate annotation files for each dialect
        GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
        dialectProvider.getLanguages()
                .stream()
                .map(dialectProvider::getDialect)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(dialect -> !unsupported.contains(dialect.getLanguage()))
                .forEach(dialectWriter::writeDialect);
    }

    static class DialectWriter {
        private final Template templateSource;
        private final String baseDirectory;
        private final String packagePath;

        DialectWriter(String baseDirectory, String packagePath) throws IOException {
            this.baseDirectory = baseDirectory;
            this.packagePath = packagePath;

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_21);
            cfg.setClassForTemplateLoading(GenerateI18n.class, "templates");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setLocale(Locale.US);
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

            templateSource = cfg.getTemplate("lambda.java.ftl");
        }

        void writeDialect(GherkinDialect dialect) {
            writeInterface(dialect);
        }

        private void writeInterface(GherkinDialect dialect) {
            String normalizedLanguage = getNormalizedLanguage(dialect);
            String languageName = dialect.getName();
            if (!dialect.getName().equals(dialect.getNativeName())) {
                languageName += " - " + dialect.getNativeName();
            }
            String className = capitalize(normalizedLanguage);

            Map<String, Object> binding = new LinkedHashMap<>();
            binding.put("className", className);
            binding.put("keywords", extractKeywords(dialect));
            binding.put("language_name", languageName);

            Path path = Paths.get(baseDirectory, packagePath, className + ".java");

            try {
                Files.createDirectories(path.getParent());
                templateSource.process(binding, newBufferedWriter(path, CREATE, TRUNCATE_EXISTING));
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        }

        // Extract sorted keywords from the dialect, and normalize them
        private static List<String> extractKeywords(GherkinDialect dialect) {
            return dialect.getStepKeywords().stream()
                    .sorted()
                    .filter(it -> !it.contains(String.valueOf('*')))
                    .filter(it -> !it.matches("^\\d.*")).distinct()
                    .map(keyword -> keyword.replaceAll("[\\s',!\u00AD]", ""))
                    .map(DialectWriter::normalize)
                    .collect(toList());
        }

        private static String capitalize(String str) {
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }

        static String normalize(CharSequence s) {
            return Normalizer.normalize(s, Normalizer.Form.NFC);
        }

        private static String getNormalizedLanguage(GherkinDialect dialect) {
            return dialect.getLanguage().replaceAll("[\\s-]", "_").toLowerCase();
        }
    }
}
