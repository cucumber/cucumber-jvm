package cuke4duke.internal.java;

public interface ObjectFactory {
    void dispose();

    Object getComponent(Class<?> type);

    void addClass(Class<?> clazz);

    void newWorld();
}
