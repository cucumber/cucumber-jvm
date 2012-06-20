package cucumber.table.xstream;

import com.thoughtworks.xstream.io.AbstractWriter;
import cucumber.table.DataTable;

import java.util.List;

public abstract class CellWriter extends AbstractWriter {
    public abstract List<String> getHeader();
    public abstract List<String> getValues();
}
