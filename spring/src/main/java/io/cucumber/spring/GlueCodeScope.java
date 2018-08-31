package io.cucumber.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

class GlueCodeScope implements Scope {
    public static final String NAME = "cucumber-glue";

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        GlueCodeContext context = GlueCodeContext.getInstance();
        Object obj = context.get(name);
        if (obj == null) {
            obj = objectFactory.getObject();
            context.put(name, obj);
        }

        return obj;
    }

    @Override
    public Object remove(String name) {
        GlueCodeContext context = GlueCodeContext.getInstance();
        return context.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        GlueCodeContext context = GlueCodeContext.getInstance();
        context.registerDestructionCallback(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        GlueCodeContext context = GlueCodeContext.getInstance();
        return context.getId();
    }
}
