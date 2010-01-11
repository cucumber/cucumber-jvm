package org.books.business;

import java.util.List;

import org.books.domain.Address;
import org.books.domain.LineItem;
import org.books.domain.PaymentInfo;

public interface PriceCalculator {

	public double getTotalPrice(List<LineItem> lineItems, Address address, PaymentInfo paymentInfo);

}