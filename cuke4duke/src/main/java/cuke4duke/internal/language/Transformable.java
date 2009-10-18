package cuke4duke.internal.language;

public interface Transformable {
    
    public <T> T transform(Class<T> returnType, Object argument) throws Throwable;

}
