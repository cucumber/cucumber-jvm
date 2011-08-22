package cucumber.runtime.java.weld;

import cucumber.runtime.java.ObjectFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.enterprise.inject.Instance;
import java.util.*;

/**
 * A CDI/Weld implementation of the Cucumber jvm ObjectFactory
 *
 * @author aaronwalker
 */
public class CDIFactory extends Weld implements ObjectFactory  {

    private Map<Class<?>,Object> instances = new HashMap<Class<?>, Object>();

    private WeldContainer weld;

    public CDIFactory() {
         weld = super.initialize();
    }

    @Override
    public void createInstances() {
    }

    @Override
    public void disposeInstances() {
    }

    @Override
    public void addClass(Class<?> clazz) {
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return weld.instance().select(type).get();
    }
}
