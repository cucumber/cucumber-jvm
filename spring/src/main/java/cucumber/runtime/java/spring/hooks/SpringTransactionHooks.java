package cucumber.runtime.java.spring.hooks;

import cucumber.annotation.After;
import cucumber.annotation.Before;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * This class defines before and after hooks which provide automatic spring rollback capabilities.
 * These hooks will apply to any element(s) within a <code>.feature</code> file tagged with <code>@txn</code>.
 * <p/>
 * Clients wishing to leverage these hooks should include this class' package in the <code>glue</code> property of the
 * Test class' {@link Cucumber.Options} annotation.
 * <p/>
 * The BEFORE and AFTER hooks both rely on being able to obtain a <code>PlatformTransactionManager</code> by type, or
 * by an optionally specified bean name, from the runtime <code>BeanFactory</code>.
 * <p/>
 * NOTE: This class is NOT threadsafe!  It relies on the fact that cucumber-jvm will instantiate an instance of any
 * applicable hookdef class per scenario run.
 */
public class SpringTransactionHooks implements BeanFactoryAware {

    private BeanFactory beanFactory;
    private String txnManagerBeanName;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * @return the (optional) bean name for the transaction manager to be obtained - if null, attempt will be made to find a transaction manager by bean type
     */
    public String getTxnManagerBeanName() {
        return txnManagerBeanName;
    }

    /**
     * Setter to allow (optional) bean name to be specified for transaction manager bean - if null, attempt will be made to find a transaction manager by bean type
     *
     * @param txnManagerBeanName bean name of transaction manager bean
     */
    public void setTxnManagerBeanName(String txnManagerBeanName) {
        this.txnManagerBeanName = txnManagerBeanName;
    }


    TransactionStatus txStatus;

    @Before({"@txn"})
    public void startTransaction() {
        txStatus = obtainPlatformTransactionManager().getTransaction(new DefaultTransactionDefinition());
    }

    @After({"@txn"})
    public void rollBackTransaction() {
        obtainPlatformTransactionManager().rollback(txStatus);
    }

    PlatformTransactionManager obtainPlatformTransactionManager() {
        if (getTxnManagerBeanName() == null) {
            return beanFactory.getBean(PlatformTransactionManager.class);
        } else {
            return beanFactory.getBean(txnManagerBeanName, PlatformTransactionManager.class);
        }
    }

}
