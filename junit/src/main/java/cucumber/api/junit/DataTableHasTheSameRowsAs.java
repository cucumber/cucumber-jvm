package cucumber.api.junit;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.TableDiffException;
import io.cucumber.datatable.TableDiffer;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.TypeSafeDiagnosingMatcher;

//TODO: Doc and examples
public class DataTableHasTheSameRowsAs extends TypeSafeDiagnosingMatcher<DataTable> {
    private final DataTable expectedValue;
    private final boolean unordered;

    private DataTableHasTheSameRowsAs(DataTable expectedValue, boolean unordered) {
        this.expectedValue = expectedValue;
        this.unordered = unordered;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a datable with the same rows");
        if (unordered) {
            description.appendText(" in any order");
        }
    }


    @Override
    protected boolean matchesSafely(DataTable item, Description description) {
        try {
            TableDiffer tableDiffer = new TableDiffer(expectedValue, item);
            if (unordered) {
                tableDiffer.calculateUnorderedDiffs();
            } else {
                tableDiffer.calculateDiffs();
            }
            return true;
        } catch (TableDiffException e) {
            description.appendText("the tables were different\n");
            description.appendText(e.getDiff().toString());
        }
        return false;
    }

    @Factory
    public static DataTableHasTheSameRowsAs hasTheSameRowsInOrderAs(DataTable operand) {
        return new DataTableHasTheSameRowsAs(operand, true);
    }

    @Factory
    public static DataTableHasTheSameRowsAs hasTheSameRowsAs(DataTable operand) {
        return new DataTableHasTheSameRowsAs(operand, false);
    }


}
