package cucumber.runtime.java.hk2;

import org.jvnet.hk2.annotations.Contract;

/**
 * Created by yorta01 on 10/1/2016.
 */
@Contract
public interface Abdomen {
    void setCukes(int n);

    int getCukes();
}
