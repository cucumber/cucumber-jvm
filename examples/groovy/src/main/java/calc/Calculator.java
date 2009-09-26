package calc;

import java.util.List;
import java.util.ArrayList;

public class Calculator {
    List<Double> stack = new ArrayList<Double>();

    public void push(double arg) {
        stack.add(arg);
    }
    
    public double divide() {
        return stack.get(0) / stack.get(1);
    }
}