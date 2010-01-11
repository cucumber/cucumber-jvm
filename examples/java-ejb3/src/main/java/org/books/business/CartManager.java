package org.books.business;

import org.books.domain.Book;
import org.books.domain.LineItem;

import java.util.List;

public interface CartManager {

    void AddBook(Book book, int quantity);

    public List<LineItem> getLineItems();

    double getTotalPrice();

    void checkout() throws Exception;

}
