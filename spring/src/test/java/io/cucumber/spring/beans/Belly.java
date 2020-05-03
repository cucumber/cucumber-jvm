package io.cucumber.spring.beans;

import org.springframework.stereotype.Component;

@Component
public class Belly {

    private int cukes = 0;

    public int getCukes() {
        return cukes;
    }

    public void setCukes(int cukes) {
        this.cukes = cukes;
    }

}
