package cucumber;

/**
 * 
 * Class used to store options for diffing tables
 * 
 */
public class TableDiffOptions {
    private boolean missingRow = true;
    
    public TableDiffOptions() {
        // Default constructor
    }
    
    public TableDiffOptions(boolean missingRow, boolean surplusRow, boolean missingCol, boolean surplusCol) {
        this.missingRow = missingRow;
        this.surplusRow = surplusRow;
        this.missingCol = missingCol;
        this.surplusCol = surplusCol;
    }

    private boolean surplusRow = true;
    private boolean missingCol = true;
    private boolean surplusCol = true;

    public boolean isMissingRow() {
        return this.missingRow;
    }

    public void setMissingRow(boolean missingRow) {
        this.missingRow = missingRow;
    }

    public boolean isSurplusRow() {
        return this.surplusRow;
    }

    public void setSurplusRow(boolean surplusRow) {
        this.surplusRow = surplusRow;
    }

    public boolean isMissingCol() {
        return this.missingCol;
    }

    public void setMissingCol(boolean missingCol) {
        this.missingCol = missingCol;
    }

    public boolean isSurplusCol() {
        return this.surplusCol;
    }

    public void setSurplusCol(boolean surplusCol) {
        this.surplusCol = surplusCol;
    }
}
