package org.books.business;

import java.util.List;

import org.books.domain.Book;
import org.books.domain.BookQuery;

public interface CatalogManager {

    public List<Book> searchBooks(BookQuery bookQuery);
	public void setMessage(String message);
	public String getMessage();
}
