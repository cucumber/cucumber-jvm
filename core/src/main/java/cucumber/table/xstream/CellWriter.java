package cucumber.table.xstream;

import cucumber.runtime.xstream.io.AbstractWriter;

import java.util.List;

public abstract class CellWriter extends AbstractWriter {
    public abstract List<String> getHeader();
    public abstract List<String> getValues();
}
