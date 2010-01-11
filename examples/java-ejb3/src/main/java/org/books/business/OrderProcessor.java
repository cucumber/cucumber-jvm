package org.books.business;

import org.books.domain.Order;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@MessageDriven
public class OrderProcessor implements MessageListener {

    @PersistenceContext(unitName = "bookstore")
    EntityManager entityManager;

    @Resource
    private TimerService timerService;

    public void onMessage(Message message) {
        try {
            String id = ((TextMessage) message).getText();

            Order order = entityManager.find(Order.class, Long.parseLong(id));
            order.setStatus(Order.Status.InProgress);

            //timerService.createTimer(500, id); // 20100102, jb: Timer works, but embedded container is not shut down properly
        } catch (JMSException e) {
            throw new EJBException(e);
        }
    }

    @Timeout
    public void closeOrder(Timer timer) throws EJBException {
        String msg = (String) timer.getInfo();
        Order order = entityManager.find(Order.class, Long.parseLong(msg));
        order.setStatus(Order.Status.Closed);
    }
}
