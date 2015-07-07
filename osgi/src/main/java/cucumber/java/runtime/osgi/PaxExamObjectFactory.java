package cucumber.java.runtime.osgi;

import org.ops4j.pax.exam.util.Injector;

public class PaxExamObjectFactory extends OsgiObjectFactoryBase {

    private Injector injector;

    public PaxExamObjectFactory(Injector injector) {
        this.injector = injector;
    }

    @Override
    protected void prepareGlueInstance(Object instance) {
        injector.injectFields(instance);
    }
}
