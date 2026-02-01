package io.cucumber.java.codegen;

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
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;

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
        private final Template packageInfoSource;
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

            templateSource = cfg.getTemplate("annotation.java.ftl");
            packageInfoSource = cfg.getTemplate("package-info.ftl");
        }

        void writeDialect(GherkinDialect dialect) {
            writeKeyWordAnnotations(dialect);
            writePackageInfo(dialect);
        }

        private void writeKeyWordAnnotations(GherkinDialect dialect) {
            dialect.getStepKeywords().stream()
                    .filter(it -> !it.contains(String.valueOf('*')))
                    .filter(it -> !it.matches("^\\d.*"))
                    .distinct()
                    .forEach(keyword -> writeKeyWordAnnotation(dialect, keyword));
        }

        private void writeKeyWordAnnotation(GherkinDialect dialect, String keyword) {
            String normalizedLanguage = getNormalizedLanguage(dialect);
            String normalizedKeyword = getNormalizedKeyWord(dialect, keyword);

            Map<String, String> binding = new LinkedHashMap<>();
            binding.put("lang", normalizedLanguage);
            binding.put("kw", normalizedKeyword);

            Path path = Paths.get(baseDirectory, packagePath, normalizedLanguage, normalizedKeyword + ".java");

            if (Files.exists(path)) {
                // Haitian has two translations that only differ by case - Sipozeke and SipozeKe
                // Some file systems are unable to distinguish between them and
                // overwrite the other one :-(
                return;
            }

            try {
                Files.createDirectories(path.getParent());
                templateSource.process(binding, newBufferedWriter(path, CREATE, TRUNCATE_EXISTING));
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        }

        private static String capitalize(String s) {
            return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
        }

        private static String getNormalizedKeyWord(GherkinDialect dialect, String keyword) {
            // Exception: Use the symbol names for the Emoj language. 
            // Emoji are not legal identifiers in Java.
            if (dialect.getLanguage().equals("em")) {
                return getNormalizedEmojiKeyWord(keyword);
            }
            return getNormalizedKeyWord(keyword);
        }
        
        private static String getNormalizedEmojiKeyWord(String keyword) {
            String titleCasedName = keyword.codePoints().mapToObj(Character::getName)
                    .map(s -> s.split(" "))
                    .flatMap(Arrays::stream)
                    .map(String::toLowerCase)
                    .map(DialectWriter::capitalize)
                    .collect(joining(" "));
            return getNormalizedKeyWord(titleCasedName);
        }

        private static String getNormalizedKeyWord(String keyword) {
            return normalize(keyword.replaceAll("[\\s',!\u00AD’]", ""));
        }

        private static String normalize(CharSequence s) {
            return Normalizer.normalize(s, Normalizer.Form.NFC);
        }

        private void writePackageInfo(GherkinDialect dialect) {
            String normalizedLanguage = getNormalizedLanguage(dialect);
            String languageName = dialect.getName();
            if (!dialect.getName().equals(dialect.getNativeName())) {
                languageName += " - " + dialect.getNativeName();
            }

            Map<String, String> binding = new LinkedHashMap<>();
            binding.put("normalized_language", normalizedLanguage);
            binding.put("language_name", languageName);

            Path path = Paths.get(baseDirectory, packagePath, normalizedLanguage, "package-info.java");

            try {
                Files.createDirectories(path.getParent());
                packageInfoSource.process(binding, newBufferedWriter(path, CREATE, TRUNCATE_EXISTING));
            } catch (IOException | TemplateException e) {
                throw new RuntimeException(e);
            }
        }

        private static String getNormalizedLanguage(GherkinDialect dialect) {
            return dialect.getLanguage().replaceAll("[\\s-]", "_").toLowerCase(Locale.ROOT);
        }

    }
}
