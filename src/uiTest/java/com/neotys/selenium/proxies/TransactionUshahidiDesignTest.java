package com.neotys.selenium.proxies;

import com.neotys.selenium.proxies.helpers.ModeHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.seleniumhq.selenium.fluent.TestableString;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.neotys.selenium.proxies.NLWebDriverFactory.addProxyCapabilitiesIfNecessary;
import static org.openqa.selenium.By.className;

/**
 * Created by paul on 11/6/17.
 */
public class TransactionUshahidiDesignTest {

    static NLWebDriver driver;
    static String baseUrl;

    @BeforeClass
    public static void before() {
        final ChromeDriver webDriver = new ChromeDriver((new ChromeOptions()).merge(addProxyCapabilitiesIfNecessary(new DesiredCapabilities())));

        driver = NLWebDriverFactory.newNLWebDriver(webDriver, MethodHandles.lookup().lookupClass().getSimpleName(), null);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

        baseUrl = ModeHelper.getSetting("baseUrl", "http://ushahidi");
    }

    @Test
    public void testPost() throws Exception {

        /*
        driver.startTransaction("Start", () -> { // if we were on Java 8

            driver.get(baseUrl + "/views/map");
            Thread.sleep(15000);
        });
        */
        driver.startTransaction("Start");
        {
            driver.get(baseUrl + "/views/map");
            //Thread.sleep(15000); // now that we have fluency, we don't need sleeps
        }
        driver.startTransaction("Click Add");
        {
            driver.fluent()
                    .button(className("button-alpha button-fab"))
                    .click();

            driver.fluent()
                    .elements(className("bug"))
                    .filter(driver.textContains("v1.2"))
                    .click();
        }
        driver.startTransaction("Click Map");
        {
            driver.fluent()
                    .element(By.cssSelector("svg.iconic")).click();
        }

        // assert that we arrived at the right end state
        assert BasicUITestSuite.elementContains(driver,
                By.cssSelector(".mode-context-title"),
                "Neotys demonstration application");

        // assert that the right things were recorded
        assert BasicUITestSuite.includesContainers(driver, new String[] {
                "Start",
                "Click Add",
                "Click Map"
        });
    }

    @AfterClass
    public static void after() {
        if (driver != null) {
            driver.quit();
        }
    }
}
