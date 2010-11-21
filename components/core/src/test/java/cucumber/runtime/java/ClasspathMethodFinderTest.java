package cucumber.runtime.java;

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

public class ClasspathMethodFinderTest {
    @Test
    public void testGetStepDefinitionMethods() throws IOException {
        ClasspathMethodScanner mf = new ClasspathMethodScanner();
        Set<Class<?>> expected = new HashSet<Class<?>> (Arrays.asList(FormatterListener.class, ParseError.class, Parser.class, StateMachineReader.class));
        assertEquals(expected, mf.getClasses("gherkin.parser"));
    }
}
