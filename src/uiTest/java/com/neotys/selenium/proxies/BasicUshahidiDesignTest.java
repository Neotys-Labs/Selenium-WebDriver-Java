package com.neotys.selenium.proxies;

import com.neotys.selenium.proxies.helpers.ModeHelper;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static com.neotys.selenium.proxies.NLWebDriverFactory.addProxyCapabilitiesIfNecessary;

/**
 * Created by paul on 11/6/17.
 */
public class BasicUshahidiDesignTest {

    static NLWebDriver driver;
    static String baseUrl;

    @BeforeClass
    public static void before() {
        final ChromeDriver webDriver = new ChromeDriver((new ChromeOptions()).merge(addProxyCapabilitiesIfNecessary(new DesiredCapabilities())));

        driver = NLWebDriverFactory.newNLWebDriver(webDriver, MethodHandles.lookup().lookupClass().getSimpleName(), null);

        baseUrl = ModeHelper.getSetting("baseUrl", "http://ushahidi");
    }

    @Test
    public void testPost() throws Exception { // flakey due to Sleeps and responsive app (but not test) design

        driver.get(baseUrl + "/views/map");
        Thread.sleep(15000);

        driver.findElement(By.cssSelector("button.button-alpha.button-fab")).click();
        Thread.sleep(1000);
        driver.findElement(By.xpath("//div[@id='bootstrap-app']/ng-view/div/main/div/post-toolbar/div/div/ul/li[1]/a/span[2]")).click();
        Thread.sleep(5000);

        driver.findElement(By.cssSelector("svg.iconic")).click();
        Thread.sleep(5000);

        // assert that we arrived at the right end state
        assert BasicUITestSuite.elementContains(driver,
                By.cssSelector(".mode-context-title"),
                "Neotys demonstration application");

        // assert that the right things were recorded
        assert BasicUITestSuite.includesContainers(driver, new String[] {
                "Navigate To: /views/map",
                "Click: Weather alert v1.0",
                "Click: svg"
        });
    }

    @AfterClass
    public static void after() {
        if (driver != null) {
            driver.quit();
        }
    }
}
