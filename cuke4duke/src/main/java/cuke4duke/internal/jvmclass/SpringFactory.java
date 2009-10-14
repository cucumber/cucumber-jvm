package cuke4duke.internal.jvmclass;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.StaticApplicationContext;

import cuke4duke.StepMother;

public class SpringFactory implements ObjectFactory {
    
    private AbstractApplicationContext appContext;
    private static ThreadLocal<StepMother> mother = new ThreadLocal<StepMother>();
    
    public void createObjects() {
        appContext.refresh();
    }

    public void disposeObjects() {
    }

    public void addClass(Class<?> clazz) {
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
	public Object getComponent(Class<?> type) {
        List beans = new ArrayList(appContext.getBeansOfType(type).values());
        if(beans.size() == 1) {
            return beans.get(0);
        } else {
            throw new RuntimeException("Found " + beans.size() + " Beans for class " + type + ". Expected exactly 1.");
        }
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
