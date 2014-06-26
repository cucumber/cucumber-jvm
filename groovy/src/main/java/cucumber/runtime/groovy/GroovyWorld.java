package cucumber.runtime.groovy;

import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Tuple;
import org.codehaus.groovy.runtime.MetaClassHelper;

import java.util.LinkedList;
import java.util.List;

class GroovyWorld extends GroovyObjectSupport {
    private final List<GroovyObject> worlds;

    public GroovyWorld() {
        super();
        worlds = new LinkedList<GroovyObject>();
    }

    public void registerWorld(Object world) {
        if (world instanceof GroovyObject) {
            worlds.add((GroovyObject) world);
        } else {
            throw new RuntimeException("Only GroovyObject supported");
        }
    }

    public Object getProperty(String property) {
        return findWorldWithProperty(property).getProperty(property);
    }

    public void setProperty(String property, Object newValue) {
        findWorldWithProperty(property).setProperty(property, newValue);
    }

    public Object invokeMethod(String name, Object args) {
        return findWorldWithMethod(name, args).invokeMethod(name, args);
    }

    int worldsCount() {
        return worlds.size();
    }

    private GroovyObject findWorldWithProperty(String property) {
        if (worlds.isEmpty()) {
            throw new MissingPropertyException(property, GroovyWorld.class);
        }

        if (worlds.size() == 1) {
            return worlds.get(0);
        }

        GroovyObject worldWithProperty = null;

        for (GroovyObject world : worlds) {
            if (world.getMetaClass().hasProperty(this, property) != null) {
                if (worldWithProperty == null) {
                    worldWithProperty = world;
                } else {
                    throw new RuntimeException("Multiple property call: " + property);
                }
            }
        }

        if (worldWithProperty == null) {
            throw new MissingPropertyException(property, GroovyWorld.class);
        }

        return worldWithProperty;
    }

    private GroovyObject findWorldWithMethod(String methodName, Object arguments) {
        Object[] args = unwrapMethodArguments(arguments);

        if (worlds.isEmpty()) {
            throw new MissingMethodException(methodName, this.getClass(), args);
        }
        if (worlds.size() == 1) {
            return worlds.get(0);
        }

        GroovyObject worldWithMethod = null;
        for (GroovyObject world : worlds) {
            if (world.getMetaClass().getMetaMethod(methodName, args) != null) {
                if (worldWithMethod == null) {
                    worldWithMethod = world;
                } else {
                    throw new RuntimeException("Multiple method call: " + methodName);
                }
            }
        }
        if (worldWithMethod == null) {
            throw new MissingMethodException(methodName, this.getClass(), args);
        }
        return worldWithMethod;
    }

    private Object[] unwrapMethodArguments(Object arguments) {
        if (arguments == null) {
            return MetaClassHelper.EMPTY_ARRAY;
        }
        if (arguments instanceof Tuple) {
            Tuple tuple = (Tuple) arguments;
            return tuple.toArray();
        }
        if (arguments instanceof Object[]) {
            return (Object[]) arguments;
        } else {
            return new Object[]{arguments};
        }
    }
}
