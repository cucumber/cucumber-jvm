package cucumber.steps;

import cucumber.app.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpringSteps extends AbstractSteps {
    @Autowired
    private HelloService helloService;

    protected HelloService getHelloService() {
        return helloService;
    }
}
