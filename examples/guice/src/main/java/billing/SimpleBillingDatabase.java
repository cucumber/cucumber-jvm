package billing;

import billing.CreateTransactionResponse.Status;

/**
 * @author Henning Jensen
 */
public class SimpleBillingDatabase implements BillingDatabase {

    public Status createTransaction(Transaction transaction) {
        return Status.OK;
    }

}
