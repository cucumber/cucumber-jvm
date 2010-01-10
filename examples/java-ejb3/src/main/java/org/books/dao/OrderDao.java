package org.books.dao;

import java.util.List;

import org.books.domain.Order;

public interface OrderDao {

	public abstract void addOrder(Order order) throws Exception;

	public abstract void deleteOrder(Order order) throws Exception;

	public abstract List<Order> getOrders();

}