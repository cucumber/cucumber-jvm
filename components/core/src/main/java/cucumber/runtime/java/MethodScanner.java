package cucumber.runtime.java;

public interface MethodScanner {
    void scan(JavaMethodBackend javaMethodBackend, String packagePrefix);
}
