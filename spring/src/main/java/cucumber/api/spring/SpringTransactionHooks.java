package cucumber.api.spring;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import io.cucumber.core.logging.Logger;
import io.cucumber.core.logging.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

/**
 * <p>
 * This class defines before and after hooks which provide automatic spring rollback capabilities.
 * These hooks will apply to any element(s) within a <code>.feature</code> file tagged with <code>@txn</code>.
 * </p>
 * <p>
 * Clients wishing to leverage these hooks should include this class' package in the <code>glue</code> code.
 * </p>
 * <p>
 * The BEFORE and AFTER hooks (both with hook order 100) rely on being able to obtain a <code>PlatformTransactionManager</code> by type, or
 * by an optionally specified bean name, from the runtime <code>BeanFactory</code>.
 * </p>
 * <p>
 * NOTE: This class is NOT threadsafe!  It relies on the fact that cucumber-jvm will instantiate an instance of any
 * applicable hookdef class per scenario run.
 * </p>
 *
 * @deprecated SpringTransactionHooks has been deprecated as it adds an unnecessary dependency on 'spring-tx'.
 * Please implement your own transaction hooks if required.
 */
@Deprecated
public class SpringTransactionHooks implements BeanFactoryAware {

    private static final Logger log = LoggerFactory.getLogger(SpringTransactionHooks.class);

    private BeanFactory beanFactory;
    private String txnManagerBeanName;
    private TransactionStatus transactionStatus;

    public SpringTransactionHooks() {
        log.warn(
            "SpringTransactionHooks has been deprecated as it adds an unnecessary dependency on 'spring-tx'. " +
                "Please implement your own transaction hooks if required."
        );
    }

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

    @Before(value = {"@txn"}, order = 100)
    public void startTransaction() {
        transactionStatus = obtainPlatformTransactionManager().getTransaction(new DefaultTransactionDefinition());
    }

    @After(value = {"@txn"}, order = 100)
    public void rollBackTransaction() {
        obtainPlatformTransactionManager().rollback(transactionStatus);
    }

    public PlatformTransactionManager obtainPlatformTransactionManager() {
        if (getTxnManagerBeanName() == null) {
            return beanFactory.getBean(PlatformTransactionManager.class);
        } else {
            return beanFactory.getBean(txnManagerBeanName, PlatformTransactionManager.class);
        }
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(SimpleTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }
}
