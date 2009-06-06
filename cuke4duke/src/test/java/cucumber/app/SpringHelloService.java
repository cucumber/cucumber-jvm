package cucumber.app;

import org.springframework.stereotype.Service;

@Service
public class SpringHelloService implements HelloService {
    public String hello() {
        return "Have a cuke, Duke";
    }
}
