package cucumber.runtime.java;

public interface ObjectFactory {
    void start();

    void stop();

    void addClass(Class<?> clazz);

    <T> T getInstance(Class<T> type);
}
