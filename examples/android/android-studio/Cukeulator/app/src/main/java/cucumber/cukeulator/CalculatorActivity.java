package cucumber.cukeulator;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CalculatorActivity extends Activity {
    private static enum Operation {ADD, SUB, MULT, DIV, NONE}

    private TextView txtCalcDisplay;
    private TextView txtCalcOperator;
    private Operation operation;
    private boolean decimals;
    private boolean resetDisplay;
    private boolean performOperation;
    private double value;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);
        txtCalcDisplay = (TextView) findViewById(R.id.txt_calc_display);
        txtCalcOperator = (TextView) findViewById(R.id.txt_calc_operator);
        operation = Operation.NONE;
    }

    public void onDigitPressed(View v) {
        if (resetDisplay) {
            txtCalcDisplay.setText(null);
            resetDisplay = false;
        }
        txtCalcOperator.setText(null);

        if (decimals || !only0IsDisplayed()) txtCalcDisplay.append(((Button) v).getText());

        if (operation != Operation.NONE) performOperation = true;
    }

    public void onOperatorPressed(View v) {
        if (performOperation) {
            performOperation();
            performOperation = false;
        }
        switch (v.getId()) {
            case R.id.btn_op_divide:
                operation = Operation.DIV;
                txtCalcOperator.setText("/");
                break;
            case R.id.btn_op_multiply:
                operation = Operation.MULT;
                txtCalcOperator.setText("x");
                break;
            case R.id.btn_op_subtract:
                operation = Operation.SUB;
                txtCalcOperator.setText("–");
                break;
            case R.id.btn_op_add:
                operation = Operation.ADD;
                txtCalcOperator.setText("+");
                break;
            case R.id.btn_op_equals:
                break;
            default:
                throw new RuntimeException("Unsupported operation.");
        }
        resetDisplay = true;
        value = getDisplayValue();
    }

    public void onSpecialPressed(View v) {
        switch (v.getId()) {
            case R.id.btn_spec_sqroot: {
                double value = getDisplayValue();
                double sqrt = Math.sqrt(value);
                txtCalcDisplay.setText(Double.toString(sqrt));
                break;
            }
            case R.id.btn_spec_pi: {
                resetDisplay = false;
                txtCalcOperator.setText(null);
                txtCalcDisplay.setText(Double.toString(Math.PI));
                if (operation != Operation.NONE) performOperation = true;
                return;
            }
            case R.id.btn_spec_percent: {
                double value = getDisplayValue();
                double percent = value / 100.0F;
                txtCalcDisplay.setText(Double.toString(percent));
                break;
            }
            case R.id.btn_spec_comma: {
                if (!decimals) {
                    String text = displayIsEmpty() ? "0." : ".";
                    txtCalcDisplay.append(text);
                    decimals = true;
                }
                break;
            }
            case R.id.btn_spec_clear: {
                value = 0;
                decimals = false;
                operation = Operation.NONE;
                txtCalcDisplay.setText(null);
                txtCalcOperator.setText(null);
                break;
            }
        }
        resetDisplay = false;
        performOperation = false;
    }

    private void performOperation() {
        double display = getDisplayValue();

        switch (operation) {
            case DIV:
                value = value / display;
                break;
            case MULT:
                value = value * display;
                break;
            case SUB:
                value = value - display;
                break;
            case ADD:
                value = value + display;
                break;
            case NONE:
                return;
            default:
                throw new RuntimeException("Unsupported operation.");
        }
        txtCalcOperator.setText(null);
        txtCalcDisplay.setText(Double.toString(value));
    }

    private boolean only0IsDisplayed() {
        CharSequence text = txtCalcDisplay.getText();
        return text.length() == 1 && text.charAt(0) == '0';
    }

    private boolean displayIsEmpty() {
        return txtCalcDisplay.getText().length() == 0;
    }

    private double getDisplayValue() {
        String display = txtCalcDisplay.getText().toString();
        return display.isEmpty() ? 0.0F : Double.parseDouble(display);
    }
}
