package billing;

import com.google.inject.AbstractModule;

/**
 * @author Henning Jensen
 */
public class DependenciesModule extends AbstractModule {

    SimpleBillingDatabase simpleBillingDatabase = new SimpleBillingDatabase();
    
    @Override
    protected void configure() {
        bind(BillingDatabase.class).toInstance(simpleBillingDatabase);
    }

}
