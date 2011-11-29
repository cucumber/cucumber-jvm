package cucumber.table.xstream;

import com.thoughtworks.xstream.io.AbstractWriter;
import cucumber.table.DataTable;

public abstract class XStreamTableWriter extends AbstractWriter {
    public abstract DataTable getDataTable();
}
