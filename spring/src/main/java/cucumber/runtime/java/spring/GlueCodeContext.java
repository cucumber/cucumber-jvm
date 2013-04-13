package cucumber.runtime.java.spring;

import java.util.HashMap;
import java.util.Map;

class GlueCodeContext {
    public static final GlueCodeContext INSTANCE = new GlueCodeContext();
    private final Map<String, Object> objects = new HashMap<String, Object>();
    private final Map<String, Runnable> callbacks = new HashMap<String, Runnable>();
    private int counter;

    private GlueCodeContext() {
    }

    public void start() {
        cleanUp();
        counter++;
    }

    public String getId() {
        return "cucumber_glue_" + counter;
    }

    public void stop() {
        for (Runnable callback : callbacks.values()) {
            callback.run();
        }
        cleanUp();
    }

    public Object get(String name) {
        return objects.get(name);
    }

    public void put(String name, Object object) {
        objects.put(name, object);
    }

    public Object remove(String name) {
        callbacks.remove(name);
        return objects.remove(name);
    }

    private void cleanUp() {
        objects.clear();
        callbacks.clear();
    }

    public void registerDestructionCallback(String name, Runnable callback) {
        callbacks.put(name, callback);
    }
}
