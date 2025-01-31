package io.cucumber.deltaspike;

import jakarta.inject.Singleton;

@Singleton
public class Belly {

    private int cukes;

    public int getCukes() {
        return cukes;
    }

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

}
