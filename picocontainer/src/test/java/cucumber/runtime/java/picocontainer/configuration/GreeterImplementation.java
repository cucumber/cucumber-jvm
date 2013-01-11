package cucumber.runtime.java.picocontainer.configuration;

public class GreeterImplementation implements GreeterInterface {

    @Override
    public String greet() {
        return "Good day!";
    }
}