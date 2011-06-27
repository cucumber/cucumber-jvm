package cucumber.runtime.java.guice;

import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cucumber.runtime.java.ObjectFactory;

import java.util.*;

public class GuiceFactory implements ObjectFactory {

    private static final String CONFIG_GUICE_MODULE = "cuke4duke.guiceModule";

    private final List<Module> modules = new ArrayList<Module>();
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();

    public GuiceFactory() throws Throwable {
       // modules.add((Module) Class.forName(moduleClassName).newInstance());
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

//    public void addStepMother(StepMother stepMother) {
//        modules.add(new StepMotherModule(stepMother));
//    }

    public void createInstances() {
        Injector injector = Guice.createInjector(modules);
        for (Class<?> clazz : classes) {
            try {
                instances.put(clazz, injector.getInstance(clazz));
            } catch (ConfigurationException e) {
                System.err.println("WARNING: Cuke4Duke/Guice could not create instance for " + clazz.getCanonicalName() + ":\n" + e.getLocalizedMessage());
            }
        }
    }

    public void disposeInstances() {
        instances.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T getInstance(Class<T> clazz) {
        return (T) instances.get(clazz);
    }

//    class StepMotherModule extends AbstractModule {
//
//        private Provider<? extends StepMother> stepMotherProvider;
//
//        public StepMotherModule(StepMother stepMother) {
//            stepMotherProvider = new StepMotherProvider(stepMother);
//        }
//
//        @Override
//        protected void configure() {
//            bind(StepMother.class).toProvider(stepMotherProvider);
//        }
//    }

//    class StepMotherProvider implements Provider<StepMother> {
//
//        private StepMother stepMother;
//
//        public StepMotherProvider(StepMother stepMother) {
//            this.stepMother = stepMother;
//        }
//
//        public StepMother get() {
//            return stepMother;
//        }
//    }

}
