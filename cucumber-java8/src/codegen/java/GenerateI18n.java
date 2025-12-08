import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialects;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static io.cucumber.core.codegen.GherkinKeywordNormalizer.capitalize;
import static io.cucumber.core.codegen.GherkinKeywordNormalizer.normalizeKeyword;
import static io.cucumber.core.codegen.GherkinKeywordNormalizer.normalizeLanguage;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.toList;

/* This class generates the cucumber-java Interfaces and package-info
 * based on the languages and keywords from the GherkinDialects
 * using the FreeMarker template engine and provided templates.
 */
public class GenerateI18n {

    // For any language that does not compile
    private static final List<String> unsupported = Collections.emptyList();

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: <baseDirectory> <packagePath>");
        }

        DialectWriter dialectWriter = new DialectWriter(args[0], args[1]);

        // Generate annotation files for each dialect
        GherkinDialects.getDialects()
                .stream()
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
			String normalizedLanguage = normalizeLanguage(dialect.getLanguage());
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
                    .filter(it -> !it.matches("^\\d.*"))
                    .distinct()
                    .map(keyword -> normalizeKeyword(dialect.getLanguage(), keyword))
                    .collect(toList());
        }
    }
}
