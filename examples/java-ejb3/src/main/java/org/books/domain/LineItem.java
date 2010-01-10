package org.books.domain;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class LineItem {
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
	
    @ManyToOne
	private Book book;
	private int quantity;
	
	public void setBook(Book book) {
		this.book = book;
	}
	public Book getBook() {
		return book;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public int getQuantity() {
		return quantity;
	}
}
