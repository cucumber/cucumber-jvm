package cucumber.runtime.java.weld;

import cucumber.runtime.CucumberException;
import cucumber.api.java.ObjectFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class WeldFactory implements ObjectFactory {

    private WeldContainer containerInstance;

    @Override
    public void start() {
        try {
            containerInstance = new Weld().initialize();
        } catch (IllegalArgumentException e) {
            throw new CucumberException("" +
                    "\n" +
                    "It looks like you're running on a single-core machine, and Weld doesn't like that. See:\n" +
                    "* http://in.relation.to/Bloggers/Weld200Alpha2Released\n" +
                    "* https://issues.jboss.org/browse/WELD-1119\n" +
                    "\n" +
                    "The workaround is to add enabled=false to a org.jboss.weld.executor.properties file on\n" +
                    "your CLASSPATH. Beware that this will trigger another Weld bug - startup will now work,\n" +
                    "but shutdown will fail with a NullPointerException. This exception will be printed and\n" +
                    "not rethrown. It's the best Cucumber-JVM can do until this bug is fixed in Weld.\n" +
                    "\n", e);
        }
    }

    @Override
    public void stop() {
        try {
            if (containerInstance.isRunning()) {
                containerInstance.close();
            }
        } catch (NullPointerException npe) {
            System.err.println("" +
                    "\nIf you have set enabled=false in org.jboss.weld.executor.properties and you are seeing\n" +
                    "this message, it means your weld container didn't shut down properly. It's a Weld bug\n" +
                    "and we can't do much to fix it in Cucumber-JVM.\n" +
                    "");
            npe.printStackTrace();
        }
    }

    @Override
    public boolean addClass(Class<?> clazz) {
        return true;
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return containerInstance.select(type).get();
    }
}
