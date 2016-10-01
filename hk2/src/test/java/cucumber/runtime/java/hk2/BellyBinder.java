package cucumber.runtime.java.hk2;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Created by yorta01 on 10/1/2016.
 */
public class BellyBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(Belly.class).to(Abdomen.class);
    }
}
