package org.books.dao;

import org.books.domain.Book;

import javax.ejb.Local;
import java.util.List;

@Local
public interface BookDao {
    void addBook(Book book) throws Exception;

    void deleteBook(Book book) throws Exception;

    List<Book> getBooks();
}
