package io.cucumber.spring.beans;

import org.springframework.stereotype.Component;

@Component
public class Belly {
    private int cukes = 0;

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

    public int getCukes() {
        return cukes;
    }
}
