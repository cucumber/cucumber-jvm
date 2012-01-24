package cucumber.runtime.java.weld;

import cucumber.runtime.java.ObjectFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class WeldFactory extends Weld implements ObjectFactory {

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
