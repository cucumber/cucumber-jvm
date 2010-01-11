package org.books.test.acceptance;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.naming.NamingException;

import org.apache.openejb.api.LocalClient;
import org.books.business.OrderManager;
import org.books.domain.LineItem;

import cuke4duke.Given;
import cuke4duke.When;

@LocalClient
public class OrderProcessingSteps extends ContainerSteps {

	@EJB private OrderManager orderManager;

	public OrderProcessingSteps(ContainerInitializer initializer) throws NamingException {
		super(initializer);
	}

	@Given("^a newly submitted order$")
	public void createNewOrder() throws Exception {
		List<LineItem> lineItems = new ArrayList<LineItem>();
		orderManager.createOrder(lineItems , null, null);
	}

	@When("^I wait ([\\d]+)s$")
	public void wait(int seconds) throws Exception {

		Thread.sleep(seconds * 1000);
	}

}
