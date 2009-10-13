package cuke4duke.internal;

import static cuke4duke.internal.Utils.join;
import cuke4duke.PyString;
import org.jruby.RubyArray;

/**
 * Converts the arguments that come from Cucumber to other types,
 * before they are sent in to step definitions.
 */
public class ArgumentsConverter {
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
        try {
            if (type.equals(Integer.TYPE)) {
                return Integer.valueOf((String) arg);
            } else if (type.equals(Long.TYPE)) {
                return Long.valueOf((String) arg);
            } else if (type.equals(Double.TYPE)) {
                return Double.valueOf((String) arg);
            } else if (type.equals(String.class)) {
                if(arg instanceof PyString) {
                    return ((PyString) arg).to_s();
                } else {
                    return String.valueOf(arg);
                }
            } else {
                return type.cast(arg);
            }
        } catch(Exception e) {
            throw new IllegalArgumentException("Sorry, cuke4duke doesn't know how to convert a " + arg + "(" + arg.getClass() + ") to type " + type, e);
        }
    }

    private void throwArgumentError(Class<?>[] types, Object[] objetcs) {
        throw new RuntimeException("Wrong number of arguments. Expected ("+join(types, ",") + ") - got (" + join(objetcs, ",") + ")");
    }
}
