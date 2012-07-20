package cucumber.runtime.java.spring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

public class WithSpringAnnotations {

    private boolean preDestroyCalled;
    private boolean postConstructCalled;
    private boolean autowired;

    @Value("${cukes.test.property}")
    private String property;

    @PostConstruct
    public void postConstruct() {
        postConstructCalled = true;
    }

    @PreDestroy
    public void preDestroy() {
        preDestroyCalled = true;
    }

    @Autowired
    public void setAutowiredCollaborator(DummyComponent collaborator) {
        autowired = true;
    }

    public boolean isAutowired() {
        return autowired;
    }

    public boolean isPostConstructCalled() {
        return postConstructCalled;
    }

    public boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }

    public String getProperty() {
        return property;
    }
}
