package cucumber.runtime;

import java.util.Collections;
import java.util.List;

class CompositeCucumberException extends CucumberException {
    private final List<Throwable> causes;

    CompositeCucumberException(List<Throwable> causes) {
        super(String.format("There were %d exceptions:", causes.size()));
        this.causes = causes;
    }

    public List<Throwable> getCauses() {
        return Collections.unmodifiableList(this.causes);
    }

    public String getMessage() {
        StringBuilder sb = new StringBuilder(super.getMessage());
        for (Throwable e : this.causes) {
            sb.append(String.format("\n  %s(%s)", e.getClass().getName(), e.getMessage()));
        }
        return sb.toString();
    }
}
