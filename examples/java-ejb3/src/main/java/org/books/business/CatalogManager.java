package org.books.business;

import org.books.domain.Book;
import org.books.domain.BookQuery;

import java.util.List;

public interface CatalogManager {

    public List<Book> searchBooks(BookQuery bookQuery);

    public void setMessage(String message);

    public String getMessage();
}
