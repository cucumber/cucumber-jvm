package cucumber.examples.java.paxexam.service;

import cucumber.examples.java.paxexam.CalculatorService;

public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public int add(int a, int b) {
        return a + b;
    }

}
