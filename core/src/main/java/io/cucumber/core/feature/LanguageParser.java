package io.cucumber.core.feature;

import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for the {@code # language: <iso-code> } header. Replaces changed ISO
 * codes for backwards compatibility.
 */
class LanguageParser {
    private static final Logger log = LoggerFactory.getLogger(FeatureParser.class);
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("^(\\s*#\\s*language\\s*:\\s*)tl");

    String parse(String feature) {
        Matcher matcher = LANGUAGE_PATTERN.matcher(feature);
        if (matcher.find()) {
            log.warn(
                () -> "The ISO 639-1 code for Telugu was changed from tl to te. Please use '# language: te' in your feature files.");
            return matcher.replaceFirst("$1te");
        }
        return feature;
    }
}
