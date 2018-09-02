package io.cucumber.spring.beans;

import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

public class PlatformTransactionManagerImpl implements PlatformTransactionManager {

    @SuppressWarnings("serial")
    @Override
    public void commit(TransactionStatus arg0) throws TransactionException {
        throw new TransactionException("commit should not be called") {
        };
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition arg0) throws TransactionException {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        return null;
    }

    @Override
    public void rollback(TransactionStatus arg0) throws TransactionException {
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

}
