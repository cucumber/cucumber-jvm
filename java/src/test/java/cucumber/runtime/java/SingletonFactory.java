package cucumber.runtime.java;

public class SingletonFactory implements ObjectFactory {
    private final Object singleton;

    public SingletonFactory(Object singleton) {
        this.singleton = singleton;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void addClass(Class<?> clazz) {
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return (T) singleton;
    }
}
