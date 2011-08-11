package cucumber.runtime.java;

public interface ObjectFactory {
    void createInstances();

    void disposeInstances();

    void addClass(Class<?> clazz);

    <T> T getInstance(Class<T> type);
}
