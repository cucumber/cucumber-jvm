package io.cucumber.java.annotation;

import io.cucumber.java.fr.Étantdonné;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FrenchSteps {

    @Étantdonné("j'ai {bigdecimal} concombres fractionnaires")
    public void jAiConcombresFractionnaires(BigDecimal arg0) {
        assertThat(arg0, is(new BigDecimal("5.5")));
    }

    @Étantdonné("j'ai {int} concombres")
    public void jAiConcombres(int arg0) {
        assertThat(arg0, is(5));
    }

}
