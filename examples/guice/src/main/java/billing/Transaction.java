package billing;

import java.math.BigDecimal;

/**
 * @author Henning Jensen
 */
public class Transaction {
    private String customerId;
    private BigDecimal amount;

    public Transaction(String customerId, BigDecimal amount) {
        this.customerId = customerId;
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction [amount=" + amount + ", customerId=" + customerId + "]";
    }

}
