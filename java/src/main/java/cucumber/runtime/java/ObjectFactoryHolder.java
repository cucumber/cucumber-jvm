package cucumber.runtime.java;

/**
 * @author rmannibucau
 */
public final class ObjectFactoryHolder {
    private static ObjectFactory factory;

    private ObjectFactoryHolder() {
        // no-op
    }

    public static ObjectFactory getFactory() {
        return factory;
    }

    public static void setFactory(ObjectFactory factory) {
        ObjectFactoryHolder.factory = factory;
    }
}
