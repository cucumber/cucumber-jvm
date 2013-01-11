package cucumber.runtime.java.picocontainer.configuration;

public class PicoConfigurerInstantiator {

    public PicoConfigurer instantiate(String configurerClassName) {
        try {
            return (PicoConfigurer) Class.forName(configurerClassName).newInstance();
        } catch (Exception e) {
            throw new PicoConfigurerInstantiationFailed(configurerClassName, e);
        }
    }

}
