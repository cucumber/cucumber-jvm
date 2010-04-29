package billing;

import billing.CreateTransactionResponse.Status;

/**
 * @author Henning Jensen
 */
public interface BillingDatabase {

    public Status createTransaction(Transaction transaction);

}
