package cucumber.runtime.java8;

import static java.lang.Class.forName;
import static java.lang.System.arraycopy;
import static jdk.internal.org.objectweb.asm.Type.getObjectType;

import cucumber.api.java8.StepdefBody;
import cucumber.runtime.CucumberException;
import cucumber.runtime.java.TypeIntrospector;
import sun.reflect.ConstantPool;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class ConstantPoolTypeIntrospector implements TypeIntrospector {
    private static final Method Class_getConstantPool;
    private static final int REFERENCE_CLASS = 0;
    private static final int REFERENCE_METHOD = 1;
    private static final int REFERENCE_ARGUMENT_TYPES = 2;

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
        final ConstantPool constantPool = (ConstantPool) Class_getConstantPool.invoke(clazz);
        final String[] member = getMemberReference(constantPool);
        final int parameterCount = interfac3.getTypeParameters().length;

        // Kotlin lambda expression without arguments or closure variables
        if (member[REFERENCE_METHOD].equals("INSTANCE")) {
            return handleKotlinInstance();
        }

        final jdk.internal.org.objectweb.asm.Type[] argumentTypes = jdk.internal.org.objectweb.asm.Type.getArgumentTypes(member[REFERENCE_ARGUMENT_TYPES]);

        // If we are one parameter short, this is a
        // - Reference to an instance method of an arbitrary object of a particular type
        if (parameterCount - 1 == argumentTypes.length) {
            return handleMethodReferenceToObjectOfType(member[REFERENCE_CLASS], handleLambda(argumentTypes, parameterCount - 1));
        }
        // If we are not short on parameters this either
        // - Reference to a static method
        // - Reference to an instance method of a particular object
        // - Reference to a constructor
        // - A lambda expression
        // We can all treat these as lambda's for figuring out the types.
        return handleLambda(argumentTypes, parameterCount);
    }

    private static Type[] handleMethodReferenceToObjectOfType(String containingType, Type[] methodArgumentTypes) throws ClassNotFoundException {
        Type[] containingTypeAndMethodArgumentTypes = new Type[methodArgumentTypes.length + 1];
        containingTypeAndMethodArgumentTypes[0] = forName(getObjectType(containingType).getClassName());
        arraycopy(methodArgumentTypes, 0, containingTypeAndMethodArgumentTypes, 1, methodArgumentTypes.length);
        return containingTypeAndMethodArgumentTypes;
    }

    private static Type[] handleLambda(jdk.internal.org.objectweb.asm.Type[] argumentTypes, int typeParameterCount) throws ClassNotFoundException {
        if (argumentTypes.length < typeParameterCount) {
            throw new CucumberException(String.format("Expected at least %s arguments but found only %s", typeParameterCount, argumentTypes.length));
        }

        // Only look at the N last arguments to the lambda static method, since the first ones might be variables
        // who only pass in the states of closed variables
        jdk.internal.org.objectweb.asm.Type[] interestingArgumentTypes = new jdk.internal.org.objectweb.asm.Type[typeParameterCount];
        arraycopy(argumentTypes, argumentTypes.length - typeParameterCount, interestingArgumentTypes, 0, typeParameterCount);

        Type[] typeArguments = new Type[typeParameterCount];
        for (int i = 0; i < typeParameterCount; i++) {
            typeArguments[i] = forName(interestingArgumentTypes[i].getClassName());
        }
        return typeArguments;
    }

    private static Type[] handleKotlinInstance() {
        return new Type[0];
    }

    private static String[] getMemberReference(ConstantPool constantPool) {
        int size = constantPool.getSize();

        // find last element in constantPool with valid memberRef
        // - previously always at size-2 index but changed with JDK 1.8.0_60
        for (int i = size - 1; i > -1; i--) {
            try {
                return constantPool.getMemberRefInfoAt(i);
            } catch (IllegalArgumentException e) {
                // eat error; null entry at ConstantPool index?
            }
        }
        throw new CucumberException("Couldn't find memberRef.");
    }

}
