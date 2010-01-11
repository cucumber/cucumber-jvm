package org.books.business;

import org.books.domain.Book;
import org.books.domain.LineItem;

import javax.ejb.EJB;
import javax.ejb.Stateful;
import java.util.ArrayList;
import java.util.List;

@Stateful
public class CartManagerImpl implements CartManager {

    @EJB
    private PriceCalculator priceCalculator;
    @EJB
    private OrderManager orderManager;

    private List<LineItem> lineItems = new ArrayList<LineItem>();

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void AddBook(Book book, int quantity) {
        LineItem lineItem = new LineItem();
        lineItem.setBook(book);
        lineItem.setQuantity(quantity);
        lineItems.add(lineItem);
    }

    public double getTotalPrice() {

        return priceCalculator.getTotalPrice(lineItems, null, null);
    }

    public void checkout() throws Exception {
        orderManager.createOrder(lineItems, null, null);
    }

}
