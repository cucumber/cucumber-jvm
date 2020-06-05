package io.cucumber.cdi2;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Belly {

    private int cukes;

    public int getCukes() {
        return cukes;
    }

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

}
