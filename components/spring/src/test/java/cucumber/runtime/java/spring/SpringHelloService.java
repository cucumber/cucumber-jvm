package cucumber.runtime.java.spring;

import org.springframework.stereotype.Service;

@Service
public class SpringHelloService {
    public String hello() {
        return "Have a cuke, Duke";
    }
}
