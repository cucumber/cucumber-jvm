package cucumber.runtime.java.advice;

import cucumber.annotation.Advice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExampleAdvice {
    private final Map<String, TimeUnit> validTimeUnits;

    public ExampleAdvice() {
        Map<String, TimeUnit> validTimeUnits = new HashMap<String, TimeUnit>();
        validTimeUnits.put("second", TimeUnit.SECONDS);
        validTimeUnits.put("seconds", TimeUnit.SECONDS);
        validTimeUnits.put("milliseconds", TimeUnit.MILLISECONDS);

        this.validTimeUnits = validTimeUnits;
    }

    /**
     * This advice waits a specified number of units and then executes the
     * original stepdef.
     *
     * This method is only called if the stepGroup leads to a valid stepdef,
     * otherwise a snippet of the original stepdef is printed.
     *
     * @param number of units to wait
     * @param unit of the number
     */
    @Advice(value = "^(\\d+) ([^\\d\\W]\\w*) have passed, (.+)$", stepGroup = 3, pointcuts =  Timed.class)
    public void timed(Runnable stepdef, int number, String unit) throws InterruptedException {
        TimeUnit timeUnit =
            checkTimeUnit(unit);
            checkNotZero(number);

        long timeout = System.currentTimeMillis() + timeUnit.toMillis(number);
        long now;

        while (timeout > (now = System.currentTimeMillis())) {
            Thread.sleep(timeout - now);
        }

        stepdef.run();
    }

    private TimeUnit checkTimeUnit(String unit) {
        TimeUnit timeUnit = validTimeUnits.get(unit);
        if (timeUnit == null) {
            throw new IllegalArgumentException(unit + " is not a valid time unit.");
        }

        return timeUnit;
    }

    private void checkNotZero(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("The specified time must be greater than zero");
        }
    }
}
