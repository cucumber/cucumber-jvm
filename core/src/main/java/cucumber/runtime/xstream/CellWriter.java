package cucumber.runtime.xstream;

import cucumber.deps.com.thoughtworks.xstream.io.AbstractWriter;

import java.util.List;

public abstract class CellWriter extends AbstractWriter {
    public abstract List<String> getHeader();

    public abstract List<String> getValues();
}
