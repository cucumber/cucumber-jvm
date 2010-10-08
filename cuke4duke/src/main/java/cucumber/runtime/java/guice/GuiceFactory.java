package cucumber.runtime.java.guice;

import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import cucumber.runtime.java.ObjectFactory;
import cuke4duke.StepMother;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GuiceFactory implements ObjectFactory {

    private static final String CONFIG_GUICE_MODULE = "cuke4duke.guiceModule";

    private final List<Module> modules = new ArrayList<Module>();
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    public GuiceFactory() throws Throwable {
        this(System.getProperty(CONFIG_GUICE_MODULE, null));
    }

    public GuiceFactory(String moduleClassName) throws Throwable {
        modules.add((Module) Class.forName(moduleClassName).newInstance());
    }

    public boolean canHandle(Class<?> clazz) {
        return Modifier.isStatic(clazz.getModifiers())
           || clazz.getEnclosingClass() == null;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother stepMother) {
        modules.add(new StepMotherModule(stepMother));
    }

    public void createObjects() {
        Injector injector = Guice.createInjector(modules);
        for (Class<?> clazz : classes) {
            try {
                instances.put(clazz, injector.getInstance(clazz));
            } catch (ConfigurationException e) {
                System.err.println("WARNING: Cuke4Duke/Guice could not create instance for " + clazz.getCanonicalName() + ":\n" + e.getLocalizedMessage());
            }
        }
    }

    public void disposeObjects() {
	      instances.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> clazz) {
        return (T) instances.get(clazz);
    }

    public Set<Class<?>> getClasses() {
        return classes;
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
