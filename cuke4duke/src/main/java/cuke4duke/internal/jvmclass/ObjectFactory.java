package cuke4duke.internal.jvmclass;

import cuke4duke.StepMother;

public interface ObjectFactory {
    void createObjects();
    void disposeObjects();
    void addClass(Class<?> clazz);
    void addStepMother(StepMother mother);
    Object getComponent(Class<?> type);
}
