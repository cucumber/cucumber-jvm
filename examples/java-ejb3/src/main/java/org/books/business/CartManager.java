package org.books.business;

import java.util.List;

import org.books.domain.Book;
import org.books.domain.LineItem;

public interface CartManager {

	void AddBook(Book book, int quantity);

	public List<LineItem> getLineItems();

	double getTotalPrice();

	void checkout() throws Exception;

}
