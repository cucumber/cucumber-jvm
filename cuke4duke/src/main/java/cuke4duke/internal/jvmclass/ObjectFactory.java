package cuke4duke.internal.jvmclass;

import cuke4duke.StepMother;

import java.util.List;

public interface ObjectFactory {
    void createObjects();

    void disposeObjects();

    boolean canHandle(Class<?> clazz);

    void addClass(Class<?> clazz);

    void addStepMother(StepMother mother);

    <T> T getComponent(Class<T> type);

    List<Class<?>> getClasses();
}
