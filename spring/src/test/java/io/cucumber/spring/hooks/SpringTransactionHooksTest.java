package io.cucumber.spring.hooks;

import cucumber.api.spring.SpringTransactionHooks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.quality.Strictness.STRICT_STUBS;

public class SpringTransactionHooksTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule().strictness(STRICT_STUBS);

    private SpringTransactionHooks target;

    @Mock
    private BeanFactory mockedBeanFactory;
    @Mock
    private PlatformTransactionManager mockedPlatformTransactionManager;

    @Before
    public void setUp() {
        target = new SpringTransactionHooks() {
            @Override
            public PlatformTransactionManager obtainPlatformTransactionManager() {
                return mockedPlatformTransactionManager;
            }
        };
        target.setBeanFactory(mockedBeanFactory);
    }

    @Test
    public void shouldObtainPlatformTransactionManagerByTypeWhenTxnManagerBeanNameNotSet() {
        SpringTransactionHooks localTarget = new SpringTransactionHooks();
        localTarget.setBeanFactory(mockedBeanFactory);

        when(mockedBeanFactory.getBean(PlatformTransactionManager.class)).thenReturn(mockedPlatformTransactionManager);

        assertSame(localTarget.obtainPlatformTransactionManager(), mockedPlatformTransactionManager);

        verify(mockedBeanFactory).getBean(PlatformTransactionManager.class);
    }

    @Test
    public void shouldObtainPlatformTransactionManagerByNameWhenTxnManagerBeanNameIsSet() {
        SpringTransactionHooks localTarget = new SpringTransactionHooks();
        localTarget.setBeanFactory(mockedBeanFactory);
        final String txnManagerBeanName = "myTxnManagerBeanName";
        localTarget.setTxnManagerBeanName(txnManagerBeanName);

        when(mockedBeanFactory.getBean(txnManagerBeanName, PlatformTransactionManager.class)).thenReturn(mockedPlatformTransactionManager);

        assertSame(localTarget.obtainPlatformTransactionManager(), mockedPlatformTransactionManager);

        verify(mockedBeanFactory).getBean(txnManagerBeanName, PlatformTransactionManager.class);
    }

    @Test
    public void shouldObtainOrStartTransactionInBeforeHook() {
        final SimpleTransactionStatus dummyTxStatus = new SimpleTransactionStatus();
        when(mockedPlatformTransactionManager.getTransaction(isA(TransactionDefinition.class))).thenReturn(dummyTxStatus);

        target.startTransaction();

        assertSame(target.getTransactionStatus(), dummyTxStatus);
    }

    @Test
    public void shouldTriggerTransactionRollbackInAfterHook() {
        final SimpleTransactionStatus dummyTxStatus = new SimpleTransactionStatus();
        target.setTransactionStatus(dummyTxStatus);

        target.rollBackTransaction();

        verify(mockedPlatformTransactionManager).rollback(dummyTxStatus);
    }

}