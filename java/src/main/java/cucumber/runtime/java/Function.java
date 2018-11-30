package cucumber.runtime.java;

public interface Function<T, R> {

    R apply(T t);

}
