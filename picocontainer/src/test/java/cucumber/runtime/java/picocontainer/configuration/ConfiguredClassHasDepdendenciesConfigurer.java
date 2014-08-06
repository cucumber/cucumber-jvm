package cucumber.runtime.java.picocontainer.configuration;


public class ConfiguredClassHasDepdendenciesConfigurer implements PicoConfigurer {

    @Override
    public void configure(PicoMapper picoMapper) {
        picoMapper.addClass(GreeterInterface.class, GreeterWithCollaborators.class);
    }

}
