package org.books.business;

import org.books.domain.Address;
import org.books.domain.LineItem;
import org.books.domain.PaymentInfo;

import java.util.List;

public interface PriceCalculator {

    public double getTotalPrice(List<LineItem> lineItems, Address address, PaymentInfo paymentInfo);

}