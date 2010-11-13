package cucumber.runtime.java.spring;

import cucumber.runtime.java.ObjectFactory;
import cuke4duke.StepMother;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpringFactory implements ObjectFactory {
    private final Set<Class<?>> classes = new HashSet<Class<?>>();
    private AbstractApplicationContext appContext;
    private static ThreadLocal<StepMother> mother = new ThreadLocal<StepMother>();

    public void createObjects() {
        appContext.refresh();
    }

    public void disposeObjects() {
    }

    public boolean canHandle(Class<?> clazz) {
        return true;
    }

    public void addClass(Class<?> clazz) {
        classes.add(clazz);
    }

    public void addStepMother(StepMother instance) {
        if (appContext == null) {
            mother.set(instance);

            StaticApplicationContext parent = new StaticApplicationContext();
            parent.registerSingleton("stepMother", StepMotherFactory.class);
            parent.refresh();

            String springXml = System.getProperty("cuke4duke.springXml", "cucumber.xml");
            appContext = new ClassPathXmlApplicationContext(new String[]{springXml}, parent);
            if (mother.get() != null) {
                throw new IllegalStateException("Expected ObjectMotherFactory to snatch up the thread local. Concurrent runs?");
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getComponent(Class<T> type) {
        List beans = new ArrayList(appContext.getBeansOfType(type).values());
        if (beans.size() == 1) {
            return (T) beans.get(0);
        } else {
            throw new RuntimeException("Found " + beans.size() + " Beans for class " + type + ". Expected exactly 1.");
        }
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

    static class StepMotherFactory implements FactoryBean, InitializingBean {
        private StepMother mother;

        public void afterPropertiesSet() throws Exception {
            this.mother = SpringFactory.mother.get();
            SpringFactory.mother.set(null);
        }

        public Object getObject() throws Exception {
            return mother;
        }

        public Class<StepMother> getObjectType() {
            return StepMother.class;
        }

        public boolean isSingleton() {
            return true;
        }
    }
}
