package simple;

import cuke4duke.annotation.I18n.ZH_CN.*;

public class ChineseCalculatorSteps {

    @假如("^我已经在计算器里输入(\\d+)$")
    public void 我已经在计算器里输入(int n) {
    }

    @当("^我按相加按钮$")
    public void 我按相加按钮() {
    }

    @那么("^我应该在屏幕上看到的结果是(\\d+)$")
    public void 我应该在屏幕上看到的结果是(int n) {
    }
}
