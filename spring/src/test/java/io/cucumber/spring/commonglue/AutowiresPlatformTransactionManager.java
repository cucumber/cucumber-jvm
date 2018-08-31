package io.cucumber.spring.commonglue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;

public class AutowiresPlatformTransactionManager {

    @Autowired
    private PlatformTransactionManager transactionManager;

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

}
