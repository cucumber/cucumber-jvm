package cucumber.runtime.java.picocontainer.configuration;


class YourPicoConfigurer implements PicoConfigurer {

    @Override
    public void configure(PicoMapper picoMapper) {
        picoMapper.addClass(GreeterInterface.class, GreeterImplementation.class);
    }
}