package cuke4duke.internal.jvmclass;

import cuke4duke.PyString;
import static cuke4duke.internal.Utils.join;
import cuke4duke.internal.language.Transformable;
import org.jruby.RubyArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Converts the arguments that come from Cucumber to other types, before they are sent in to step definitions.
 */
public class ArgumentsConverter {

    private final Map<Class<?>, Transformable> transforms;

    public ArgumentsConverter(Map<Class<?>, Transformable> transforms) {
        this.transforms = createDefaultTransformations();
        this.transforms.putAll(transforms);
    }

    private HashMap<Class<?>, Transformable> createDefaultTransformations() {
        return new HashMap<Class<?>, Transformable>(new DefaultJvmTransforms().createDefaultJvmTransforms());
    }

    public Object[] convert(Class<?>[] types, RubyArray args) {
        return convert(types, args.toArray());
    }

    public Object[] convert(Class<?>[] types, Object[] objetcs) {
        if (types.length != objetcs.length) {
            throwArgumentError(types, objetcs);
        }
        Object[] converted = new Object[objetcs.length];
        for (int i = 0; i < types.length; i++) {
            converted[i] = convertObject(types[i], objetcs[i]);
        }
        return converted;
    }

    private Object convertObject(Class<?> type, Object arg) {
        if(type.isAssignableFrom(arg.getClass())) {
            return arg;
        }
        try {
            if (type.equals(String.class)) {
                if (arg instanceof PyString) {
                    return ((PyString) arg).to_s();
                } else {
                    return String.valueOf(arg);
                }
            } else {
                Transformable transform = transforms.get(type);
                if (transform != null)
                    return transform.transform(type, arg);
                else
                    return type.cast(arg);
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Sorry, cuke4duke doesn't know how to convert a " + arg + " (" + arg.getClass() + ") to type " + type, e);
        }
    }

    private void throwArgumentError(Class<?>[] types, Object[] objetcs) {
        throw new RuntimeException("Wrong number of arguments. Expected (" + join(types, ",") + ") - got (" + join(objetcs, ",") + ")");
    }

}
