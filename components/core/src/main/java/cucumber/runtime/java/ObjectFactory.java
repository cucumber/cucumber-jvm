package cucumber.runtime.java;

import cuke4duke.StepMother;

import java.util.Set;

public interface ObjectFactory {
    void createObjects();

    void disposeObjects();

    boolean canHandle(Class<?> clazz);

    void addClass(Class<?> clazz);

    void addStepMother(StepMother mother);

    <T> T getComponent(Class<T> type);

    Set<Class<?>> getClasses();
}
