package cuke4duke.internal.jvmclass;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import cuke4duke.StepMother;

/**
 * @author Henning Jensen
 */
public class GuiceFactory implements ObjectFactory {

	private final Module module;
    private final List<Class<?>> classes = new ArrayList<Class<?>>();
    private final Map<Class<?>, Object> instanceMap = new HashMap<Class<?>, Object>();
    private Injector injector;

    public GuiceFactory() throws Throwable {
        String moduleClassName = System.getProperty("cuke4duke.guiceModule", null);
        module = (Module) Class.forName(moduleClassName).newInstance();
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother mother) {
        // Not supported yet.
    }

    public void createObjects() {
        injector = Guice.createInjector(module);
		for(Class<?> clazz: classes) {
	        instanceMap.put(clazz, injector.getInstance(clazz));
		}
    }

    public void disposeObjects() {
    }

    public Object getComponent(Class<?> clazz) {
        return instanceMap.get(clazz);
    }

}
