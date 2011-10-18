package cucumber.runtime.java.spring.hooks;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import cucumber.annotation.After;
import cucumber.annotation.Before;

public class SpringTransactionHooks {

    TransactionStatus txStatus;
    
    private PlatformTransactionManager platformTransactionManager;
    public PlatformTransactionManager getPlatformTransactionManager() {
        return platformTransactionManager;
    }
    public void setPlatformTransactionManager(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }
    
    @Before({"@txn"})
    public void rollBackBeforeHook() {
        txStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
    }
    
    @After({"@txn"})
    public void rollBackAfterHook() {
        platformTransactionManager.rollback(txStatus);
    }
    
}

