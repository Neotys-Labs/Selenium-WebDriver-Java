package com.neotys.selenium.proxies;

import com.neotys.selenium.proxies.helpers.ModeHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.TimeUnit;

import static com.neotys.selenium.proxies.NLWebDriverFactory.addProxyCapabilitiesIfNecessary;
import static org.openqa.selenium.By.className;

/**
 * Created by paul on 11/6/17.
 */
public class GoogleTest {

    static NLWebDriver driver;

    @BeforeClass
    public static void before() {
        final ChromeDriver webDriver = new ChromeDriver((new ChromeOptions()).merge(addProxyCapabilitiesIfNecessary(new DesiredCapabilities())));

        driver = NLWebDriverFactory.newNLWebDriver(webDriver, MethodHandles.lookup().lookupClass().getSimpleName(), null);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void testPost() throws Exception {

        driver.get("https://www.google.com");

        driver.fluent()
                .input(By.name("q"))
                .clearField()
                .sendKeys("Neotys")
                .sendKeys(Keys.ENTER);

        driver.startTransaction("Neotys site search"); {
            Actions builder = new Actions(driver);
            WebElement element = driver.findElement(By.xpath("//input[contains(@title, 'Search neotys')]"));
            builder.moveToElement(element)
                    .click()
                    .sendKeys("DevOps")
                    .sendKeys(Keys.ENTER)
                    .build().perform();
        }

        // assert that we arrived at the right end state
        assert driver.fluent()
                .img(By.id("logo"))
                .isDisplayed().value();

        // assert that the right things were recorded
        assert BasicUITestSuite.includesContainers(driver, new String[] {
                "Navigate To: www.google.com",
                "Neotys site search"
        });
    }

    @AfterClass
    public static void after() {
        if (driver != null) {
            driver.quit();
        }
    }
}
