package cucumber.runtime.java;

import java.util.Set;

public interface ObjectFactory {
    void createObjects();

    void disposeObjects();

    boolean canHandle(Class<?> clazz);

    void addClass(Class<?> clazz);

    <T> T getComponent(Class<T> type);

    Set<Class<?>> getClasses();
}
