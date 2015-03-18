package cucumber.runtime.java;

import java.util.HashMap;
import java.util.Map;

class Java8Snippet extends AbstractJavaSnippet {
    private static final Map<Class<?>, Class<?>> PRIMITIVES_TO_WRAPPERS = new HashMap<Class<?>, Class<?>>() {{
        put(boolean.class, Boolean.class);
        put(byte.class, Byte.class);
        put(char.class, Character.class);
        put(double.class, Double.class);
        put(float.class, Float.class);
        put(int.class, Integer.class);
        put(long.class, Long.class);
        put(short.class, Short.class);
        put(void.class, Void.class);
    }};

    @Override
    protected String getArgType(Class<?> argType) {
        if (argType.isPrimitive()) {
            return PRIMITIVES_TO_WRAPPERS.get(argType).getSimpleName();
        }
        return argType.getSimpleName();
    }

    @Override
    public String template() {
        return "{0}(\"{1}\", ({3}) -> '{'\n" +
                "    // {4}\n" +
                "{5}    throw new PendingException();\n" +
                "'}');\n";
    }
}
