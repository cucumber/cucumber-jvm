package cucumber.runtime.java;

public class SingletonFactory implements ObjectFactory {
    private final Object singleton;

    public SingletonFactory(Object singleton) {
        this.singleton = singleton;
    }

    @Override
    public void createInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disposeInstances() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addClass(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getInstance(Class<T> type) {
        return (T) singleton;
    }
}
