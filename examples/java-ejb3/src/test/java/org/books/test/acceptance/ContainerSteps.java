package org.books.test.acceptance;

import javax.naming.Context;
import javax.naming.NamingException;

public abstract class ContainerSteps {

    protected static Context context;

    public ContainerSteps(ContainerInitializer initializer) throws NamingException {
        // constructor-injection by cuke4duke/picocontainer
        context = initializer.getContext();
        context.bind("inject", this);
    }

}