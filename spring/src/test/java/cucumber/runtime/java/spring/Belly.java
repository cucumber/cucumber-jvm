package cucumber.runtime.java.spring;

import org.springframework.stereotype.Component;

@Component
public class Belly {
    private int cukes;

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

    public int getCukes() {
        return cukes;
    }
}
