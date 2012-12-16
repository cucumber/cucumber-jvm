package cucumber.examples.java.wicket.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;

public class RentACarSupport {
    public void createCars(int availableCars) {
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get("http://localhost:8080/rentit/create");

            WebElement numberOfCarsToCreate = driver.findElement(By.id("numberOfCars"));
            numberOfCarsToCreate.clear();
            numberOfCarsToCreate.sendKeys("" + availableCars);

            WebElement createButton = driver.findElement(By.id("createButton"));
            createButton.click();
        } finally {
            driver.close();
        }
    }

    public void rentACar() {
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get("http://localhost:8080/rentit/rent");

            WebElement rentButton = driver.findElement(By.id("rentButton"));
            rentButton.click();
        } finally {
            driver.close();
        }
    }

    public int getAvailableNumberOfCars() {
        WebDriver driver = new FirefoxDriver();
        try {
            driver.get("http://localhost:8080/rentit/available");

            WebElement availableCars = driver.findElement(By.id("availableCars"));
            String availableCarsString = availableCars.getText();

            return Integer.parseInt(availableCarsString);
        } finally {
            driver.close();
        }
    }
}
