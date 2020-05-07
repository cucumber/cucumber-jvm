package io.cucumber.examples.spring.txn;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * <p>
 * This class defines before and after hooks which provide automatic spring
 * rollback capabilities. These hooks will apply to any element(s) within a
 * <code>.feature</code> file tagged with <code>@txn</code>.
 * </p>
 * <p>
 * Clients wishing to leverage these hooks should include a copy of this class'
 * in their <code>glue</code> code.
 * </p>
 * <p>
 * The BEFORE and AFTER hooks (both with hook order 100) rely on being able to
 * obtain a <code>PlatformTransactionManager</code> by type, or by an optionally
 * specified bean name, from the runtime <code>BeanFactory</code>.
 * </p>
 * <p>
 * NOTE: This class is NOT threadsafe! It relies on the fact that cucumber-jvm
 * will instantiate an instance of any applicable hookdef class per scenario
 * run.
 * </p>
 */
public class SpringTransactionHooks implements BeanFactoryAware {

    private BeanFactory beanFactory;
    private TransactionStatus transactionStatus;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Before(value = "@txn", order = 100)
    public void startTransaction() {
        transactionStatus = obtainPlatformTransactionManager()
                .getTransaction(new DefaultTransactionDefinition());
    }

    public PlatformTransactionManager obtainPlatformTransactionManager() {
        return beanFactory.getBean(PlatformTransactionManager.class);
    }

    @After(value = "@txn", order = 100)
    public void rollBackTransaction() {
        obtainPlatformTransactionManager()
                .rollback(transactionStatus);
    }

}
