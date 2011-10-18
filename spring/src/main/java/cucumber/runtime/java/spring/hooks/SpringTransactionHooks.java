package cucumber.runtime.java.spring.hooks;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class SpringTransactionHooks {

    TransactionStatus txStatus;

    private PlatformTransactionManager txMgr;

    /**
     * Setter for Dependency Injection - autowiring would rely on too many client-specific details
     */
    public void setTxMgr(PlatformTransactionManager txMgr) {
        this.txMgr = txMgr;
    }


    @Before({"@txn"})
    public void rollBackBeforeHook() {
        txStatus = txMgr.getTransaction(new DefaultTransactionDefinition());
    }

    @After({"@txn"})
    public void rollBackAfterHook() {
        txMgr.rollback(txStatus);
    }

}

