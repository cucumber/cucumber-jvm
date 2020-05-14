package io.cucumber.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

class CucumberScenarioScope implements Scope {

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        CucumberTestContext context = CucumberTestContext.getInstance();
        Object obj = context.get(name);
        if (obj == null) {
            obj = objectFactory.getObject();
            context.put(name, obj);
        }

        return obj;
    }

    @Override
    public Object remove(String name) {
        CucumberTestContext context = CucumberTestContext.getInstance();
        return context.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        CucumberTestContext context = CucumberTestContext.getInstance();
        context.registerDestructionCallback(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        CucumberTestContext context = CucumberTestContext.getInstance();
        return context.getId();
    }

}
