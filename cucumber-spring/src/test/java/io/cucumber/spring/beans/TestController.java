package io.cucumber.spring.beans;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Component
@RequestMapping(TestController.BASE_URL)
public class TestController {

    public static final String BASE_URL = "/test";

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public String test() {
        return "ok";
    }

}
