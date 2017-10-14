package cucumber.java.step;

import cucumber.api.DataTable;
import cucumber.java.Table;

import java.util.ArrayList;
import java.util.List;

public class InvokeArgs {
    private List<String> args = new ArrayList<String>();
    private Table tableArg = new Table();

    public InvokeArgs() { }

    public void addArg(String arg) {
        args.add(arg);
    }

    public List<String> getInvokeArgs() {
        return args;
    }

    public Table getTableArg() {
        return tableArg;
    }
}
