package cucumber.runtime.java.weld;

import cucumber.runtime.CucumberException;
import cucumber.api.java.ObjectFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class WeldFactory
        implements ObjectFactory {

    protected static final String LINE_SEPARATOR = System.lineSeparator();

    protected static final String START_EXCEPTION_MESSAGE = "" +
        LINE_SEPARATOR +
        "It looks like you're running on a single-core machine, and Weld doesn't like that. See:" +
        LINE_SEPARATOR +
        "* http://in.relation.to/Bloggers/Weld200Alpha2Released" +
        LINE_SEPARATOR +
        "* https://issues.jboss.org/browse/WELD-1119" +
        LINE_SEPARATOR +
        LINE_SEPARATOR +
        "The workaround is to add enabled=false to a org.jboss.weld.executor.properties file on" +
        LINE_SEPARATOR +
        "your CLASSPATH. Beware that this will trigger another Weld bug - startup will now work," +
        LINE_SEPARATOR +
        "but shutdown will fail with a NullPointerException. This exception will be printed and" +
        LINE_SEPARATOR +
        "not rethrown. It's the best Cucumber-JVM can do until this bug is fixed in Weld." +
        LINE_SEPARATOR +
        LINE_SEPARATOR;

    protected static final String STOP_EXCEPTION_MESSAGE = "" +
        LINE_SEPARATOR +
        "If you have set enabled=false in org.jboss.weld.executor.properties and you are seeing" +
        LINE_SEPARATOR +
        "this message, it means your weld container didn't shut down properly. It's a Weld bug" +
        LINE_SEPARATOR +
        "and we can't do much to fix it in Cucumber-JVM." +
        LINE_SEPARATOR;

    private WeldContainer containerInstance;

    @Override
    public void start() {
        start(null);
    }

    protected void start(Weld weld) {
        try {
            if (weld == null) {
                weld = new Weld();
            }
            containerInstance = weld.initialize();
        } catch (IllegalArgumentException e) {
            throw new CucumberException(START_EXCEPTION_MESSAGE, e);
        }
    }

    @Override
    public void stop() {
        try {
            if (containerInstance.isRunning()) {
                containerInstance.close();
            }
        } catch (NullPointerException npe) {
            System.err.println(STOP_EXCEPTION_MESSAGE);
            npe.printStackTrace(System.err);
        }
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return containerInstance.select(type)
            .get();
    }

}
