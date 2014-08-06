package cucumber.runtime.java.picocontainer.configuration;


public class PrivateConstructor implements PicoConfigurer {

    private PrivateConstructor() {
    }

    @Override
    public void configure(PicoMapper picoMapper) {
        throw new RuntimeException("Configure should not be called for: " + this.getClass().getName());
    }

}
