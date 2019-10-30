import io.github.bonigarcia.seljup.SeleniumExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SeleniumExtension.class)
public class TestSuite {


    private ChromeDriver driver;
    private JavascriptExecutor js;
    private String BaseURL = "https://www.w3schools.com/sql/trysql.asp?filename=trysql_select_all";
    private WebElement runButton;


    public WebElement searchContactName(String name) {
        By searchContactNameLocator = By.xpath(String.format("//td[3][text() = '%s']", name));
        return driver.findElement(searchContactNameLocator);
    }

    public WebElement searchPersonAddress(String name) {
        searchContactName(name);
        By personAddress = By.xpath(String.format("//td[3][text() = '%s']/following-sibling::td[1]", name));
        return driver.findElement(personAddress);
    }

    public int howManyRecords() {
        js.executeScript("window.editor.setValue('SELECT COUNT (*) FROM Customers')");
        runButton.click();
        int rowCounter = Integer.parseInt(driver.findElementByXPath("//div[@id='divResultSQL']//table//td").getText());
        return rowCounter;
    }

    // Driver injection
    public TestSuite(ChromeDriver driver) {
        this.driver = driver;
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        this.js = (JavascriptExecutor) driver;
        driver.get(BaseURL);
        this.runButton = (new WebDriverWait(driver, 15))
                .until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[onclick^='w3schoolsSQLSubmit']")));

    }


    @Test
    @DisplayName("Check person's address")
    public void testPersonsAddress() {
        js.executeScript("window.editor.setValue('SELECT * FROM Customers')");
        runButton.click();

        String personName = "Giovanni Rovelli";
        searchContactName(personName);
        String actualAddress = searchPersonAddress(personName).getText();

        assertEquals("Via Ludovico il Moro 22", actualAddress, "Wrong address!");
    }

    @Test
    @DisplayName("Show only \"London\" records")
    public void testLondonRecords() {
        js.executeScript("window.editor.setValue('SELECT * FROM Customers where City=\\'London\\'')");
        runButton.click();

        int rowsActual = driver.findElementsByCssSelector("#divResultSQL tr").size() - 1;

        assertEquals(6, rowsActual, "Wrong table size!");
    }

    @Test
    @DisplayName("RowUpdate")
    public void testRowUpdate() throws Exception {
        js.executeScript("window.editor.setValue('UPDATE Customers " +
                "SET CustomerName=\\'Ivan Petrov\\'," +
                " ContactName=\\'Ivan\\', " +
                "Address=\\'None\\', " +
                "City=\\'London\\', " +
                "PostalCode=\\'454000\\'" +
                " where CustomerID=\\'2\\'')");
        runButton.click();
        String actualMessage = driver.findElementByXPath("//div[contains(text(),'You have made changes')]").getText();
        assertEquals("You have made changes to the database. Rows affected: 1", actualMessage,
                "There was no message about changes in the database.");


    }

    @Test
    @DisplayName("Add new record")
    public void testAddRecord() {
        int beforeCount = howManyRecords();

        js.executeScript("window.editor.setValue('INSERT INTO Customers" +
                " (CustomerName, ContactName, Address, City, PostalCode, Country)" +
                " VALUES (\"Maxim I. Rabinovich\",\"Max Rabi\",\"111, Lector st.\",\"Neverland\",\"454000\",\"NE\")')");
        runButton.click();
        int afterCount = howManyRecords();

        assertEquals(beforeCount + 1, afterCount, "Wrong number of records");
    }

    @Test
    @DisplayName("Try to drop database")
    public void testTableDropping(){
        int beforeCount = howManyRecords();

        js.executeScript("window.editor.setValue('DROP DATABASE Customer')");
        runButton.click();

        try {
            Alert alert = (new WebDriverWait(driver, 20))
                    .until(ExpectedConditions.alertIsPresent());
            alert.dismiss();
        } catch (Exception e) {
        }

        int afterCount = howManyRecords();

        assertEquals(beforeCount, afterCount, "Database DROPPED!!!");
    }
}

