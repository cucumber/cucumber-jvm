package cucumber.runtime.formatter;

import cucumber.api.event.EventPublisher;
import cucumber.api.formatter.ColorAware;
import cucumber.api.formatter.Formatter;

public class PrettyFormatter implements Formatter, ColorAware {
    private boolean monochrome;

    public PrettyFormatter(Appendable out) {
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
    }

    @Override
    public void setMonochrome(boolean monochrome) {
        this.monochrome = monochrome;
    }
}
