package cuke4duke.internal.language;

public interface Transformable {
    public <T> T transform(Object argument) throws Throwable;

}
