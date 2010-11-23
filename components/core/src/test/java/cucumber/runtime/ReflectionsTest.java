package cucumber.runtime;

import gherkin.formatter.FilterFormatter;
import gherkin.formatter.Formatter;
import gherkin.formatter.JSONFormatter;
import gherkin.formatter.PrettyFormatter;
import gherkin.parser.FormatterListener;
import gherkin.parser.ParseError;
import gherkin.parser.Parser;
import gherkin.parser.StateMachineReader;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class ReflectionsTest {
    @Test
    public void looksUpClassesOnClassPath() throws IOException {
        Set<Class<?>> expected = new HashSet<Class<?>> (Arrays.asList(FormatterListener.class, ParseError.class, Parser.class, StateMachineReader.class));
        assertEquals(expected, Classpath.getClasses("gherkin.parser"));
    }

    @Test
    public void looksUpSubclassesOnClassPath() throws IOException {
        Set<Class<? extends Formatter>> expected = new HashSet<Class<? extends Formatter>> (Arrays.asList(JSONFormatter.class, PrettyFormatter.class, FilterFormatter.class));
        assertEquals(expected, Classpath.getSubtypesOf(Formatter.class, "gherkin"));
    }

}
