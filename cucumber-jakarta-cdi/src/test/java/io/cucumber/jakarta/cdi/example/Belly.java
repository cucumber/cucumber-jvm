package io.cucumber.jakarta.cdi.example;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@SuppressWarnings("DesignForExtension")
public class Belly {

    private int cukes;

    public int getCukes() {
        return cukes;
    }

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

}
