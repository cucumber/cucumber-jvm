package io.cucumber.examples.java.wicket.steps;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class RentACarSupport {

    public static final String BASE_URL = "http://localhost:9878/java-wicket-main/";

    public void createCars(int availableCars) {
        WebDriver driver = new HtmlUnitDriver();
        try {
            driver.get(BASE_URL + "create");

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
        WebDriver driver = new HtmlUnitDriver();
        try {
            driver.get(BASE_URL + "rent");

            WebElement rentButton = driver.findElement(By.id("rentButton"));
            rentButton.click();
        } finally {
            driver.close();
        }
    }

    public int getAvailableNumberOfCars() {
        WebDriver driver = new HtmlUnitDriver();
        try {
            driver.get(BASE_URL + "available");

            WebElement availableCars = driver.findElement(By.id("availableCars"));
            String availableCarsString = availableCars.getText();

            return Integer.parseInt(availableCarsString);
        } finally {
            driver.close();
        }
    }

}
