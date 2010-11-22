package org.books.test.acceptance;

import cucumber.annotation.annotation.I18n.EN.Then;
import cucumber.annotation.annotation.I18n.EN.When;
import org.apache.openejb.api.LocalClient;
import org.books.business.CartManager;
import org.books.domain.Book;

import javax.ejb.EJB;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

@LocalClient
public class ShoppingcartSteps extends ContainerSteps {

    @EJB
    private CartManager cartManager;

    public ShoppingcartSteps(ContainerInitializer initializer) throws NamingException {
        super(initializer);
    }

    @When("^I put a book with price (.+) into my shopping cart$")
    public void iPutABookIntoMyShoppingCart(double price) {
        Book book = new Book();
        book.setPrice(price);
        cartManager.AddBook(book, 1);
    }

    @Then("^my shopping cart should contain ([\\d]+) line items?$")
    public void myShoppingCartShouldContainLineItems(int count) {

        assertEquals("Shopping cart contains wrong count.", count, cartManager.getLineItems().size());
    }

    @Then("^the total price should be (.+)$")
    public void theTotalPriceShouldBe(double price) {

        assertEquals("Shopping cart contains wrong count.", price, cartManager.getTotalPrice(), 0);
    }
}
