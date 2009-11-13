package cuke4duke.internal.jvmclass;

import cuke4duke.Transform;
import cuke4duke.internal.language.Transformable;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class DefaultJvmTransforms implements Transformable {

    private Map<Class<?>, Method> methods;
    private Map<Class<?>, Transformable> transforms;

    public DefaultJvmTransforms() {
        this.methods = new HashMap<Class<?>, Method>();
        this.transforms = new HashMap<Class<?>, Transformable>();
    }

    @Transform
    public Object transformStringToObject(String argument) {
        return argument;
    }

    @Transform
    public int transformStringToInt(String argument) {
        return Integer.valueOf(argument);
    }

    @Transform
    public Integer transformStringToInteger(String argument) {
        return Integer.valueOf(argument);
    }

    @Transform
    public long transformStringToLongPrimitive(String argument) {
        return Long.valueOf(argument);
    }

    @Transform
    public Long transformStringToLong(String argument) {
        return Long.valueOf(argument);
    }

    @Transform
    public double transformStringToDoublePrimitive(String argument) {
        return Double.valueOf(argument);
    }

    @Transform
    public Double transformStringToDouble(String argument) {
        return Double.valueOf(argument);
    }

    @Transform
    public float transformStringToFloatPrimitive(String argument) {
        return Float.valueOf(argument);
    }

    @Transform
    public Float transformStringToFloat(String argument) {
        return Float.valueOf(argument);
    }

    @Transform
    public short transformStringToShortPrimitive(String argument) {
        return Short.valueOf(argument);
    }

    @Transform
    public Short transformStringToShort(String argument) {
        return Short.valueOf(argument);
    }

    @Transform
    public byte transformStringToBytePrimitive(String argument) {
        return Byte.valueOf(argument);
    }

    @Transform
    public Byte transformStringToByte(String argument) {
        return Byte.valueOf(argument);
    }

    @Transform
    public char transformStringToChar(String argument) {
        return Character.valueOf(argument.charAt(0));
    }

    @Transform
    public Character transformStringToCharacters(String argument) {
        return Character.valueOf(argument.charAt(0));
    }

    @Transform
    public BigDecimal transformStringToBigDecimal(String argument) {
        return BigDecimal.valueOf(Double.valueOf(argument));
    }

    @Transform
    public BigInteger transformStringToBigInteger(String argument) {
        return BigInteger.valueOf(Long.valueOf(argument));
    }

    @Transform
    public boolean transformStringToBooleanPrimitive(String argument) {
        return Boolean.valueOf(argument);
    }

    @Transform
    public Boolean transformStringToBoolean(String argument) {
        return Boolean.valueOf(argument);
    }

    public Map<Class<?>, Transformable> createDefaultJvmTransforms() {
        for (Method method : DefaultJvmTransforms.class.getMethods()) {
            if (method.isAnnotationPresent(Transform.class))
                methods.put(method.getReturnType(), method);
            transforms.put(method.getReturnType(), this);
        }
        return transforms;
    }

    @SuppressWarnings("unchecked")
    public <T> T transform(Class<T> returnType, Object argument) throws Throwable {
        Method method = methods.get(returnType);
        if (method == null)
            throw new IllegalArgumentException();
        return (T) method.invoke(this, argument);
    }

}
