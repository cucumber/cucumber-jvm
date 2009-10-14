package billing;

import billing.CreateTransactionResponse.Status;

import com.google.inject.Inject;

/**
 * @author Henning Jensen
 */
public class BillingService {

    private BillingDatabase database;

    @Inject
    public BillingService(BillingDatabase database) {
        this.database = database;
    }

    public CreateTransactionResponse sendTransactionToBilling(Transaction transaction) {
        Status status = database.createTransaction(transaction);
        return new CreateTransactionResponse(status, "OK");
    }

}
