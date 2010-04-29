package simple;

import org.springframework.stereotype.Component;

@Component
public class MyGreeter implements Greeter {
    public String hello() {
        return "Have a cuke, Duke!";
    }
}
