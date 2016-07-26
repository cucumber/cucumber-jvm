package cucumber.runtime.java8;

import cucumber.runtime.CucumberException;
import cucumber.runtime.java.TypeIntrospector;
import sun.reflect.ConstantPool;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public class ConstantPoolTypeIntrospector implements TypeIntrospector {
    private static final Method Class_getConstantPool;

    static {
        try {
            Class_getConstantPool = Class.class.getDeclaredMethod("getConstantPool");
            Class_getConstantPool.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final TypeIntrospector INSTANCE = new ConstantPoolTypeIntrospector();

    @Override
    public Type[] getGenericTypes(Class<?> clazz) throws Exception {
        Type[] typeArguments;
        ConstantPool constantPool = (ConstantPool) Class_getConstantPool.invoke(clazz);
        String typeString = getTypeString(constantPool);
        jdk.internal.org.objectweb.asm.Type[] argumentTypes = jdk.internal.org.objectweb.asm.Type.getArgumentTypes(typeString);
        typeArguments = new Type[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            typeArguments[i] = Class.forName(argumentTypes[i].getClassName());
        }
        return typeArguments;
    }

    private String getTypeString(ConstantPool constantPool) {
        int size = constantPool.getSize();
        String[] memberRef = null;

        // find last element in constantPool with valid memberRef
        // - previously always at size-2 index but changed with JDK 1.8.0_60
        for (int i = size - 1; i > -1; i--) {
            try {
                memberRef = constantPool.getMemberRefInfoAt(i);
                break;
            } catch (IllegalArgumentException e) {
                // eat error; null entry at ConstantPool index?
            }
        }

        return memberRef[2];
    }

}
