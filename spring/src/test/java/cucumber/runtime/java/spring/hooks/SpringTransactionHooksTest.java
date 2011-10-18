package cucumber.runtime.java.spring.hooks;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.SimpleTransactionStatus;

@RunWith(MockitoJUnitRunner.class)
public class SpringTransactionHooksTest {

	private AbstractSpringTransactionHooks target;
	
	@Mock
	private PlatformTransactionManager mockedPlatformTransactionManager;
	
	@Before
	public void setUp() {
		target = new AbstractSpringTransactionHooks();
		target.setPlatformTransactionManager(mockedPlatformTransactionManager);
	}
	
	@Test
	public void shouldObtainOrStartTransactionInBeforeHook() {
		final SimpleTransactionStatus dummyTxStatus = new SimpleTransactionStatus();
		when(mockedPlatformTransactionManager.getTransaction(isA(TransactionDefinition.class))).thenReturn(dummyTxStatus);
		
		target.rollBackBeforeHook();
		
		assertSame(target.txStatus, dummyTxStatus);
	}
	
	@Test
	public void shouldTriggerTransactionRollbackInAfterHook() {
		final SimpleTransactionStatus dummyTxStatus = new SimpleTransactionStatus();
		target.txStatus = dummyTxStatus;
		
		mockedPlatformTransactionManager.rollback(dummyTxStatus);
		
		verify(mockedPlatformTransactionManager).rollback(dummyTxStatus);
	}


}
