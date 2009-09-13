package cuke4duke.internal.jvmclass;

public interface ObjectFactory {
    void disposeObjects();

    Object getComponent(Class<?> type);

    void addClass(Class<?> clazz);

    void createObjects();
}
