package cucumber.runtime.java.picocontainer;

import org.picocontainer.Parameter;
import org.picocontainer.parameters.ConstantParameter;
import org.picocontainer.parameters.DefaultConstructorParameter;

import java.util.HashMap;
import java.util.Map;

public class ConstructorParameters
{
    private final static Map<Class<?>, Parameter[]> CONSTRUCTORS = new HashMap<Class<?>, Parameter[]>();
    static
    {
        CONSTRUCTORS.put(String.class, constructor());
        CONSTRUCTORS.put(Integer.class, constructor(0));
        CONSTRUCTORS.put(Long.class, constructor((long) 0));
        CONSTRUCTORS.put(Double.class, constructor(0.0d));
        CONSTRUCTORS.put(Float.class, constructor(0.0f));
        CONSTRUCTORS.put(Character.class, constructor('\u0000'));
        CONSTRUCTORS.put(Short.class, constructor((short) 0));
        CONSTRUCTORS.put(Byte.class, constructor((byte) 0));
    }

    private static Parameter[] constructor(Object value)
    {
        return new Parameter[] { new ConstantParameter(value) };
    }

    private static Parameter[] constructor()
    {
        return new Parameter[] { DefaultConstructorParameter.INSTANCE };
    }

    static Parameter[] getConstructorFor(Class<?> clazz)
    {
        return CONSTRUCTORS.get(clazz);
    }
}
