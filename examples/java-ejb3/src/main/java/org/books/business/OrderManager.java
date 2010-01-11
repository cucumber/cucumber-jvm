package org.books.business;

import java.util.List;

import org.books.domain.Address;
import org.books.domain.LineItem;
import org.books.domain.PaymentInfo;

public interface OrderManager {

	public void createOrder(List<LineItem> lineItems, Address address, PaymentInfo paymentInfo) throws Exception;

}