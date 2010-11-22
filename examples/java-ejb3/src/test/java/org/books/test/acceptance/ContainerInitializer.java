package org.books.test.acceptance;

import cucumber.annotation.annotation.After;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class ContainerInitializer {

    private final Context context;

    public ContainerInitializer() throws NamingException {
        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
        p.put("bookstoreDatabase", "new://Resource?type=DataSource");
        p.put("bookstoreDatabase.JdbcDriver", "org.hsqldb.jdbcDriver");
        p.put("bookstoreDatabase.JdbcUrl", "jdbc:hsqldb:mem:moviedb");

        p.put("openejb.embedded.initialcontext.close", "destroy");
        p.put("openejb.tempclassloader.skip", "annotations");

        context = new InitialContext(p);
    }

    public Context getContext() {
        return context;
    }

    @After
    public void shutdown() throws Exception {
        context.close();
    }
}
