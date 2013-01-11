package cucumber.runtime.java.picocontainer.configuration;

import static java.text.MessageFormat.format;

public class PicoConfigurerInstantiationFailed extends RuntimeException {

    private static final long serialVersionUID = 975206168851096186L;

    public PicoConfigurerInstantiationFailed(String configurerClassName, Exception e) {
        super(format("Instantiation of ''{0}'' failed", configurerClassName), e);
    }

}
