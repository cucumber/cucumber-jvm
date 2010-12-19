package cucumber.runtime.java;

public interface ObjectFactory {
    void createInstances();
    void disposeInstances();
    boolean canHandle(Class<?> clazz);
    void addClass(Class<?> clazz);
    <T> T getInstance(Class<T> type);
}
