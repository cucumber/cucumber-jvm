package cuke4duke.internal.language;

public interface Transformable {
    
    public Class<?> transform(Class<?> returnType, Object argument) throws Throwable;

}
