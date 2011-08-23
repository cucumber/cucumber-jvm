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

    private WeldContainer weld;

    @Override
    public void createInstances() {
    	weld = super.initialize();
    }

    @Override
    public void disposeInstances() {
    	this.shutdown();
    }

    @Override
    public void addClass(Class<?> clazz) {
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return weld.instance().select(type).get();
    }
}
