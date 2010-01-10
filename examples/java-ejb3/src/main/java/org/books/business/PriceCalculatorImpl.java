package org.books.business;

import java.util.List;

import javax.ejb.Stateless;

import org.books.domain.Address;
import org.books.domain.LineItem;
import org.books.domain.PaymentInfo;

@Stateless 
public class PriceCalculatorImpl implements PriceCalculator {
	
	public double getTotalPrice(List<LineItem> lineItems, Address address, PaymentInfo paymentInfo) {
		double totalPrice = 0;
		for (LineItem lineItem : lineItems){
			totalPrice += lineItem.getQuantity() * lineItem.getBook().getPrice();
		}
		return totalPrice;
	}
	
}
