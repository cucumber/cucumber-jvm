package cucumber.runtime.java;

class SingletonFactory implements ObjectFactory {
    private Object singleton;

    public SingletonFactory() {
        this(null);
    }

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
        if (singleton == null) {
            throw new IllegalStateException("No object is set");
        }
        return type.cast(singleton);
    }

    public void setInstance(Object o) {
        singleton = o;
    }
}
