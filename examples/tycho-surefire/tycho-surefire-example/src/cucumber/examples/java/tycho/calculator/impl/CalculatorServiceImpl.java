package cucumber.examples.java.tycho.calculator.impl;

import cucumber.examples.java.tycho.calculator.api.CalculatorService;

public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

}
