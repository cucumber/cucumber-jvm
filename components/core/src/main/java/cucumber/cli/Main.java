package cucumber.cli;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.BaseConverter;
import cucumber.Runtime;
import cucumber.runtime.Backend;
import cucumber.runtime.java.JavaMethodBackend;
import cucumber.runtime.java.ObjectFactory;
import cucumber.runtime.java.ReflectionsMethodFinder;
import gherkin.formatter.Formatter;
import gherkin.formatter.PrettyFormatter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * Command line interface around {@link cucumber.Runtime}
 */
public class Main {
    @Parameter
    private List<String> features = null;

    @Parameter(names = "--stepdefs", required = true)
    public String stepDefPackage;

    @Parameter(names = "--factory", converter = ObjectFactoryConverter.class)
    public ObjectFactory objectFactory;

    public void execute(Writer out) throws IOException {
        if (objectFactory == null)
            objectFactory = new ObjectFactoryConverter("--factory").convert("pico");

        Backend backend = new JavaMethodBackend(objectFactory, new ReflectionsMethodFinder(stepDefPackage));
        Formatter formatter = new PrettyFormatter(out, true, true);
        Runtime runtime = new Runtime(backend, formatter);
        runtime.execute(features);
    }

    public static void main(String[] argv) throws IOException {
        mainWithWriter(new OutputStreamWriter(System.out), argv);
    }

    static void mainWithWriter(Writer out, String... argv) throws IOException {
        Main main = new Main();
        new JCommander(main, argv);
        main.execute(out);
    }


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
