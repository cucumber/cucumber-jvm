package cucumber.runtime.java.spring.contexthierarchyconfig;

import cucumber.runtime.java.spring.beans.DummyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;

@ContextHierarchy({
    @ContextConfiguration("classpath:cucumber2.xml"),
    @ContextConfiguration("classpath:cucumber.xml")
    })
public class WithContextHierarchyAnnotation {

    private boolean autowired;

    @Autowired
    public void setAutowiredCollaborator(DummyComponent collaborator) {
        autowired = true;
    }

    public boolean isAutowired() {
        return autowired;
    }

}
