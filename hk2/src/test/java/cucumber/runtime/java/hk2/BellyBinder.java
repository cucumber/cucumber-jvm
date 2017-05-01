package cucumber.runtime.java.hk2;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class BellyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(Belly.class).to(Abdomen.class);
    }
}
