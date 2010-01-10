package org.books.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.books.domain.Order;

@Stateless
public class OrderDaoImpl implements OrderDao {

    @PersistenceContext(unitName = "bookstore")
    private EntityManager entityManager;

    public void addOrder(Order order) throws Exception {
        entityManager.persist(order);
    }

    public void deleteOrder(Order order) throws Exception {
        entityManager.remove(order);
    }

    public List<Order> getOrders() {
        Query query = entityManager.createQuery("SELECT o from Order as o");
        return query.getResultList();
    }

}
