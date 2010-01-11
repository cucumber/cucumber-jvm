package org.books.dao;

import java.util.List;

import javax.ejb.Local;

import org.books.domain.Book;

@Local
public interface BookDao {
    void addBook(Book book) throws Exception ;

    void deleteBook(Book book) throws Exception ;

    List<Book> getBooks();
}
