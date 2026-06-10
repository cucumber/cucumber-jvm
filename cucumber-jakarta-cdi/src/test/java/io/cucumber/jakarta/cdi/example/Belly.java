package io.cucumber.jakarta.cdi.example;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
class Belly {

    private int cukes;

    int getCukes() {
        return cukes;
    }

    void setCukes(int cukes) {
        this.cukes = cukes;
    }

}
