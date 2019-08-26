package cucumber.runtime.io;

public interface ResourceLoader {
    Iterable<Resource> resources(String path, String suffix);
}
