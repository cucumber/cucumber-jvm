package org.books.dao;

import org.books.domain.Order;

import java.util.List;

public interface OrderDao {

    public abstract void addOrder(Order order) throws Exception;

    public abstract void deleteOrder(Order order) throws Exception;

    public abstract List<Order> getOrders();

}