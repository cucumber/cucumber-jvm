package cuke4duke.internal.jvmclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;

import cuke4duke.StepMother;

/**
 * @author Henning Jensen
 */
public class GuiceFactory implements ObjectFactory {

    private static final String CONFIG_GUICE_MODULE = "cuke4duke.guiceModule";

    private final List<Module> modules = new ArrayList<Module>();
    private final List<Class<?>> classes = new ArrayList<Class<?>>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
    private Injector injector;

    public GuiceFactory() throws Throwable {
        String moduleClassName = System.getProperty(CONFIG_GUICE_MODULE, null);
        modules.add((Module) Class.forName(moduleClassName).newInstance());
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother stepMother) {
        modules.add(new StepMotherModule(stepMother));
    }

    public void createObjects() {
        injector = Guice.createInjector(modules);
        for (Class<?> clazz : classes) {
        	try {
        		instances.put(clazz, injector.getInstance(clazz));
			} catch (ConfigurationException e) {
				System.err.println("Could not create instance for "+clazz.getCanonicalName()+":\n"+e.getLocalizedMessage());
			}
        }
    }

    public void disposeObjects() {
    }

    public Object getComponent(Class<?> clazz) {
        return instances.get(clazz);
    }

    class StepMotherModule extends AbstractModule {

        private Provider<? extends StepMother> stepMotherProvider;

        public StepMotherModule(StepMother stepMother) {
            stepMotherProvider = new StepMotherProvider(stepMother);
        }

        @Override
        protected void configure() {
            bind(StepMother.class).toProvider(stepMotherProvider);
        }
    }

    class StepMotherProvider implements Provider<StepMother> {

        private StepMother stepMother;

        public StepMotherProvider(StepMother stepMother) {
            this.stepMother = stepMother;
        }

        public StepMother get() {
            return stepMother;
        }
    }

}
