package io.cucumber.spring.componentannotation;

import io.cucumber.spring.beans.DummyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class WithControllerAnnotation {

    private boolean autowired;

    @Autowired
    public void setAutowiredCollaborator(DummyComponent collaborator) {
        autowired = true;
    }

    public boolean isAutowired() {
        return autowired;
    }

}
