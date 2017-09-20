package cucumber.runtime.java.spring.componentannotation;

import cucumber.runtime.java.spring.beans.DummyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;

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
