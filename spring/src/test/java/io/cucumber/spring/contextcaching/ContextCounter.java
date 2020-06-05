package io.cucumber.spring.contextcaching;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class ContextCounter implements ApplicationContextAware {

    private static final Set<ApplicationContext> applicationContextSet = new HashSet<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        applicationContextSet.add(applicationContext);
    }

    public int getContextCount() {
        return applicationContextSet.size();
    }

}
