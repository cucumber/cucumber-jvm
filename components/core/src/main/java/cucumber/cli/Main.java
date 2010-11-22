package cucumber.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import cucumber.Cucumber;
import cucumber.runtime.Backend;
import cucumber.runtime.java.ClasspathMethodScanner;
import cucumber.runtime.java.JavaBackend;
import cucumber.runtime.java.ObjectFactory;
import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Command line interface around {@link cucumber.Cucumber}
 */
public class Main {
    @Parameter
    private List<String> features = null;

    @Parameter(names = "--stepdefs", required = true)
    public String packagePrefix;

    @Parameter(names = "--factory", converter = ObjectFactoryConverter.class)
    public ObjectFactory objectFactory;

    public void execute(Writer out) throws IOException {
        if (objectFactory == null)
            objectFactory = new ObjectFactoryConverter("--factory").convert("pico");

        Backend backend = new JavaBackend(objectFactory, new ClasspathMethodScanner(), packagePrefix);
        Formatter formatter = new PrettyFormatter(out, true, true);
        Cucumber cucumber = new Cucumber(backend, formatter);
        cucumber.execute(features);
    }

    public static void main(String[] argv) throws IOException {
        mainWithWriter(new OutputStreamWriter(System.out), argv);
    }

    static void mainWithWriter(Writer out, String... argv) throws IOException {
        Main main = new Main();
        new JCommander(main, argv);
        main.execute(out);
    }

    // TODO: Make this optional and guess factory from current classpath. Use generic code from MethodFinder:
    // Set<Class<ObjectFactory>> factories = classFinder.getClasses()...
    public class ObjectFactoryConverter extends BaseConverter<ObjectFactory> {

        public ObjectFactoryConverter(String optionName) {
            super(optionName);
        }

        public ObjectFactory convert(String s) {
            String className = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() + "Factory";
            String fqcn = "cucumber.runtime.java." + s + "." + className;
            try {
                Class<ObjectFactory> clazz = (Class<ObjectFactory>) getClass().getClassLoader().loadClass(fqcn);
                return clazz.getConstructor().newInstance();
            } catch (Exception e) {
                throw new ParameterException(getErrorString(s, e.getMessage()));
            }
        }
    }

}
