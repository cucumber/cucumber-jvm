package cucumber.runtime.java.spring;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

class GlueCodeScope implements Scope {
    public static final String NAME = "cucumber-glue";

    private final GlueCodeContext context = GlueCodeContext.INSTANCE;

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        Object obj = context.get(name);
        if (obj == null) {
            obj = objectFactory.getObject();
            context.put(name, obj);
        }

        return obj;
    }

    @Override
    public Object remove(String name) {
        return context.remove(name);
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        context.registerDestructionCallback(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return context.getId();
    }
}
