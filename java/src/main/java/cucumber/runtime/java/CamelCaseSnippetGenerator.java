package cucumber.runtime.java;

import cucumber.api.DataTable;
import cucumber.runtime.snippets.ArgumentPattern;
import cucumber.runtime.snippets.Snippet;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.I18n;
import gherkin.formatter.model.Step;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CamelCaseSnippetGenerator extends SnippetGenerator {

    public CamelCaseSnippetGenerator(Snippet snippet) {
        super(snippet);
    }

    @Override
    protected String sanitizeFunctionName(String functionName) {

        StringBuilder sanitized = new StringBuilder();

        String trimmedFunctionName = functionName.trim();

        if(!Character.isJavaIdentifierStart(trimmedFunctionName.charAt(0))) {
            sanitized.append("_");
        }

        String[] words = trimmedFunctionName.split(" ");

        sanitized.append(words[0].toLowerCase());

        for(int i=1; i<words.length; i++) {
            sanitized.append(capitalize(words[i].toLowerCase()));
        }

        return sanitized.toString();
    }

    private String capitalize(String line)
    {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }
}
