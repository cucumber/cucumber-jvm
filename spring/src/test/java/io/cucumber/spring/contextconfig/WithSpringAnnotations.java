package io.cucumber.spring.contextconfig;

import io.cucumber.spring.beans.DummyComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:cucumber.xml")
public class WithSpringAnnotations {

    private boolean autowired;

    @Value("${cukes.test.property}")
    private String property;

    @Autowired
    public void setAutowiredCollaborator(DummyComponent collaborator) {
        autowired = true;
    }

    public boolean isAutowired() {
        return autowired;
    }

    public String getProperty() {
        return property;
    }

}
