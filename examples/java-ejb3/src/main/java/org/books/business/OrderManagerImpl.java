package org.books.business;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.books.dao.OrderDao;
import org.books.domain.Address;
import org.books.domain.LineItem;
import org.books.domain.Order;
import org.books.domain.PaymentInfo;

@Stateless
public class OrderManagerImpl implements OrderManager {

	@PersistenceContext(unitName = "bookstore")
	EntityManager entityManager;

	@Resource
	ConnectionFactory connectionFactory;
	@Resource(name = "OrderProcessor")
	Queue orderQueue;
	@EJB private OrderDao orderDao;
	@EJB private PriceCalculator priceCalculator;

	public void createOrder(List<LineItem> lineItems, Address address, PaymentInfo paymentInfo) throws Exception {

		double price = priceCalculator.getTotalPrice(lineItems, address, paymentInfo);

		Order order = new Order();
		for (LineItem lineItem : lineItems) {
			order.getLineItems().add(lineItem);
		}
		order.setPrice(price);

		orderDao.addOrder(order);
		
		sendOrder(order.getId());
	}

	public void sendOrder(long orderId) throws JMSException {

		Connection connection = null;
		Session session = null;

		try {
			connection = connectionFactory.createConnection();
			connection.start();

			// Create a Session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			// Create a MessageProducer from the Session to the Topic or Queue
			MessageProducer producer = session.createProducer(orderQueue);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			// Create a message
			TextMessage message = session.createTextMessage(Long.toString(orderId));

			// Tell the producer to send the message
			producer.send(message);
		} finally {
			// Clean up
			if (session != null)
				session.close();
			if (connection != null)
				connection.close();
		}
	}

}
