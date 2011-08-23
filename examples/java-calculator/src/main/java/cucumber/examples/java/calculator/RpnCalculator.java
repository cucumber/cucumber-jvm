package cucumber.examples.java.calculator;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class RpnCalculator {
    private final List<Number> stack = new ArrayList<Number>();
    private static final List<String> OPS = asList("-", "+", "*", "/");

    public void push(Object arg) {
        if (OPS.contains(arg)) {
            Number y = stack.remove(stack.size() - 1);
            Number x = stack.remove(stack.size() - 1);
            Double val = null;
            if (arg.equals("-")) {
                val = x.doubleValue() - y.doubleValue();
            } else if (arg.equals("+")) {
                val = x.doubleValue() + y.doubleValue();
            } else if (arg.equals("*")) {
                val = x.doubleValue() * y.doubleValue();
            } else if (arg.equals("/")) {
                val = x.doubleValue() / y.doubleValue();
            }
            push(val);
        } else {
            stack.add((Number) arg);
        }
    }

    public void PI() {
        push(Math.PI);
    }

    public Number value() {
        return stack.get(stack.size() - 1);
    }
}
