package cuke4duke.steps;

import cuke4duke.app.HelloService;

public class PicoContainerSteps extends AbstractSteps {
    private final HelloService helloService;

    public PicoContainerSteps(HelloService helloService) {
        this.helloService = helloService;
    }

    protected HelloService getHelloService() {
        return helloService;
    }
}
