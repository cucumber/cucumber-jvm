package cucumber.runtime;

import gherkin.formatter.*;
import gherkin.formatter.Formatter;
import gherkin.parser.FormatterListener;
import gherkin.parser.ParseError;
import gherkin.parser.Parser;
import gherkin.parser.StateMachineReader;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ClasspathTest {
    @Test
    public void looksUpClassesOnClassPath() throws IOException {
        Set<Class<?>> expected = new HashSet<Class<?>> (Arrays.asList(FormatterListener.class, ParseError.class, Parser.class, StateMachineReader.class));
        assertEquals(expected, Classpath.getPublicClasses("gherkin.parser"));
    }

    @Test
    public void looksUpSubclassesOnClassPath() throws IOException {
        Set<Class<? extends Formatter>> expected = new HashSet<Class<? extends Formatter>> (Arrays.asList(JSONFormatter.class, PrettyFormatter.class, FilterFormatter.class, Reporter.class));
        assertEquals(expected, Classpath.getPublicSubclassesOf(Formatter.class, "gherkin"));
    }

    @Test
    public void looksUpFilesByDir() throws IOException {
        final List<String> paths = new ArrayList<String>();
        Classpath.scan("cucumber/runtime", ".xyz", new Consumer() {
            public void consume(Input input) throws IOException {
                paths.add(input.getPath());
            }
        });
        assertEquals(Arrays.asList("cucumber/runtime/bar.xyz", "cucumber/runtime/foo.xyz"), paths);
    }

    @Test
    public void looksUpFilesByFile() throws IOException {
        final List<String> paths = new ArrayList<String>();
        Classpath.scan("cucumber/runtime/foo.xyz", new Consumer() {
            public void consume(Input input) throws IOException {
                paths.add(input.getPath());
            }
        });
        assertEquals(Arrays.asList("cucumber/runtime/foo.xyz"), paths);
    }
}
