package cucumber.runtime.java.hk2;

import org.jvnet.hk2.annotations.Service;

@Service
public class Belly implements Abdomen {
    private int cukes;

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

    public int getCukes() {
        return cukes;
    }
}
