package cucumber.runtime.java.hk2;

import cucumber.api.java.ObjectFactory;
import cucumber.runtime.Utils;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.api.ServiceLocatorState;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yorta01 on 9/23/2016.
 */
public class Hk2Factory implements ObjectFactory {
    ServiceLocator serviceLocator;

    private final String serviceName = "cucumber";

    private final Set<Class<?>> classes = new HashSet<Class<?>>();

    Set<Class<?>> binderClasses = new HashSet<Class<?>>();
    private boolean populate = true;

    @Override
    public void start() {
        // start is NOT the first method called.
        // start is called once per scenario
        //System.out.println("In Hk2Factory start!");

        serviceLocator = getServiceLocator();
        ServiceLocatorUtilities.addClasses(serviceLocator, classes.toArray(new Class<?>[0]));

        // okay, now add the binders to the serviceLocator!
        addBinders(serviceLocator, binderClasses);
    }

    private void addBinders(ServiceLocator serviceLocator, Set<Class<?>> binderClasses) {
        Set<Binder> binderInstances = new HashSet<Binder>();
        try {
            for (Class<?> currentClass : binderClasses) {
                binderInstances.add((Binder) currentClass.newInstance());
            }
        } catch (Exception e) {

        }
        ServiceLocatorUtilities.bind(serviceLocator, binderInstances.toArray(new Binder[0]));

    }

    @Override
    public void stop() {
        // stop is called once per scenario
        //System.out.println("In Hk2Factory stop!");
        if (serviceLocator != null) {
            serviceLocator.shutdown();
        }
    }

    @Override
    public boolean addClass(Class<?> glueClass) {
        //System.out.println(String.format("In Hk2Factory addClass(%s)!", glueClass.toString()));
        if (glueClass == null || glueClass.getName().equals("int") || glueClass.getName().equals("boolean")) {
            return false;
        }

        if (Utils.isInstantiable(glueClass) && classes.add(glueClass)) {
            addBinders(glueClass);
            addSuperClass(glueClass);
            addInterfaceDependencies(glueClass);
            addConstructorDependencies(glueClass);
            addFieldDependencies(glueClass);
            getPopulateSetting(glueClass);
        }
        return true;
    }

    private void addSuperClass(Class<?> clazz) {
        // also add the super class
        Class<?> superclazz = clazz.getSuperclass();
        if (superclazz != null && !superclazz.getName().equals("java.lang.Object")) {
            addClass(superclazz);
        }
    }

    private void addConstructorDependencies(Class<?> clazz) {
        // constructor injection
        for (Constructor constructor : clazz.getDeclaredConstructors()) {
            for (Class paramClazz : constructor.getParameterTypes()) {
                addClass(paramClazz);
            }
        }
    }

    private void addInterfaceDependencies(Class<?> clazz) {
        // not sure if we need this
        for (Class<?> interfacee : clazz.getInterfaces()) {
            addClass(interfacee);
        }
    }

    private void addFieldDependencies(Class<?> clazz) {
        // field injection
        for (Field fields : clazz.getDeclaredFields()) {
            addClass(fields.getType());
        }
    }

    @Override
    public <T> T getInstance(Class<T> glueClass) {
        //System.out.println(String.format("In Hk2Factory getInstance(%s)!", glueClass.toString()));

        T glueClassInstance = getServiceLocator().getService(glueClass);
        return glueClassInstance;
    }

    private ServiceLocator getServiceLocator() {
        // first see if it already exists
        if (serviceLocator == null || serviceLocator.getState().equals(ServiceLocatorState.SHUTDOWN)) {
            // it doesn't exist, or it was previously shutdown
            if (populate) {
                // create service locator and
                // 'populate' the HK2 locator with classes from META-INF/hk2-locator/default.
                serviceLocator = ServiceLocatorUtilities.createAndPopulateServiceLocator(serviceName);
            } else {
                serviceLocator = ServiceLocatorFactory.getInstance().create(serviceName);
            }
        }
        return serviceLocator;
    }

    private void getPopulateSetting(Class<?> clazz) {
        // if it's already false, no need to check
        if (clazz != null || populate == false) {
            Hk2Binders hk2Binders = clazz.getAnnotation(Hk2Binders.class);

            if (hk2Binders != null) {
                populate = hk2Binders.populate();
            }
        }
    }

    private void addBinders(Class<?> clazz) {
        if (clazz != null) {
            Hk2Binders hk2Binders = clazz.getAnnotation(Hk2Binders.class);

            if (hk2Binders != null) {
                //System.out.println(String.format("Hk2Binders annotation found in (%s)!", clazz.toString()));
                Class<? extends Binder>[] hk2BinderClasses = hk2Binders.binders();

                if (hk2BinderClasses.length > 0) {
                    for (Class<? extends Binder> binderClass : hk2BinderClasses) {
                        binderClasses.add(binderClass); // add to list of ALL binders
                    }
                }
            }

        }
    }
}


