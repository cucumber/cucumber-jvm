package cucumber.runtime.java.spring.hooks;

import cucumber.annotation.Pending;

/**
 * Implements an interface to be able to use jdk 1.4 proxies at runtime, instead of cglib.
 */
public class PendingBelly implements Belly {
	
	public static final String NO_BELLY = "TODO: I have not cuke storage in my belly :(";
	
    @Pending(NO_BELLY)
    public void setCukes(int cukes) {
    }
    
    @Pending(NO_BELLY)
    public int getCukes() {
    	return 0;
    }

}
