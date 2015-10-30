package cucumber.runtime.java8;

import cucumber.runtime.CucumberException;
import cucumber.api.java8.StepdefBody;
import cucumber.runtime.java.TypeIntrospector;
import sun.reflect.ConstantPool;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
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
    public Type[] getGenericTypes(Class<? extends StepdefBody> clazz, Class<? extends StepdefBody> interfac3) throws Exception {
        ConstantPool constantPool = (ConstantPool) Class_getConstantPool.invoke(clazz);
        String typeString = getLambdaTypeString(constantPool);
        int typeParameterCount = interfac3.getTypeParameters().length;
        jdk.internal.org.objectweb.asm.Type[] argumentTypes = jdk.internal.org.objectweb.asm.Type.getArgumentTypes(typeString);
        // Only look at the N last arguments to the lambda static method, since the first ones might be variables
        // who only pass in the states of closed variables
        List<jdk.internal.org.objectweb.asm.Type> interestingArgumentTypes = Arrays.asList(argumentTypes)
                .subList(argumentTypes.length - typeParameterCount, argumentTypes.length);

        Type[] typeArguments = new Type[typeParameterCount];
        for (int i = 0; i < typeParameterCount; i++) {
            typeArguments[i] = Class.forName(interestingArgumentTypes.get(i).getClassName());
        }
        return typeArguments;
    }

    private String getLambdaTypeString(ConstantPool constantPool) {
        int size = constantPool.getSize();
        String[] memberRef = null;

        // find last element in constantPool with valid memberRef
        // - previously always at size-2 index but changed with JDK 1.8.0_60
        for (int i = size - 1; i > -1; i--) {
            try {
                memberRef = constantPool.getMemberRefInfoAt(i);
		return memberRef[2];
            } catch (IllegalArgumentException e) {
                // eat error; null entry at ConstantPool index?
            }
        }
	throw new CucumberException("Couldn't find memberRef.");
    }

}
