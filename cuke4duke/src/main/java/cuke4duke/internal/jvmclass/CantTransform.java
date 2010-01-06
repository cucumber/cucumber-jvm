package cuke4duke.internal.jvmclass;

public class CantTransform extends RuntimeException {
    public CantTransform(Object arg, Class<?> parameterType) {
        super("Can't transform " + arg + " (" + arg.getClass() + ") to " + parameterType + ". You should declare an explicit Transform");
    }
}
