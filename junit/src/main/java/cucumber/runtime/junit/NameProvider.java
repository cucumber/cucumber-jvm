package cucumber.runtime.junit;

import org.junit.runner.Description;

import gherkin.formatter.model.Examples;
import gherkin.formatter.model.TagStatement;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.CucumberTagStatement;

/**
 * This class provides a name for a {@link Description} for the various
 * cucumber elements (feature, scenario, scenario outline).
 * 
 * 
 * In order to work properly in the Eclipse IDE, this name has to have the 
 * following properties:
 * 
 * <ul>
 * <li>The name has to be unique accross all features</li>
 * <li>The name can't contain parenthesis</li>
 * </ul>
 *
 */
public class NameProvider {

    private final CucumberFeature feature;
    
    public NameProvider(CucumberFeature feature) {
        this.feature = feature;
    }
    
    public String getName() {
        return replaceParenthesis(feature.getGherkinFeature().getName());
    }
    
    public String getName(CucumberTagStatement cucumberTagStatement) {
        String info = cucumberTagStatement.getVisualName().startsWith("|") ?
            " <" + cucumberTagStatement.getVisualName() + ">" : "";
        return getName(cucumberTagStatement.getGherkinModel(), info);
    }
    
    private String getName(TagStatement statement, String info) {
        String name = statement.getName();
        if (name == null || name.length() == 0) {
            name = statement.getKeyword();
        }
        return replaceParenthesis(name + info + getSuffix(statement));
    }

    private String getSuffix(TagStatement statement) {
        return " [" + feature.getUri() + ":" + statement.getLine() + "]";
    }
    
    private static String replaceParenthesis(String name) {
        return name.replace('(', '<').replace(')','>');
    }

    public String getName(Examples examples) {
        return getName(examples, "");
    }

}
