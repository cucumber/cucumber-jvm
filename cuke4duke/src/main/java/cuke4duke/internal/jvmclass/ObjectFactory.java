package cuke4duke.internal.jvmclass;

public interface ObjectFactory {
    void createObjects();
    void disposeObjects();
    void addClass(Class<?> clazz);
    Object getComponent(Class<?> type);
}
