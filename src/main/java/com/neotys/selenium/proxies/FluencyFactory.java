package com.neotys.selenium.proxies;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.seleniumhq.selenium.fluent.FluentWebDriver;
import org.seleniumhq.selenium.fluent.FluentWebElement;
import org.seleniumhq.selenium.fluent.Period;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Created by paul on 11/7/17.
 */
public class FluencyFactory {

    // create a fluent web driver that injects control visibility wait timing to overcome async DOM manipulation delays
    public static FluentWebDriver createFluentWebDriver(final WebDriver delegate, final int timeoutInSeconds)
    {
        // proxy the underlying FluentWebDriver object to inject method handlers
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(FluentWebDriver.class);
        factory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(Method method) {
                return method.getReturnType().equals(FluentWebElement.class);
            }
        });

        final Period timeoutPeriod = Period.secs(timeoutInSeconds);

        final WebDriverWait wait = new WebDriverWait(delegate, timeoutInSeconds);

        MethodHandler handler = new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                boolean attach = thisMethod.getReturnType().equals(FluentWebElement.class);

                // wait until messages from various frameworks have been processed (concept taken from Espresso fw)
                if(attach && delegate.getClass().isAssignableFrom(JavascriptExecutor.class))
                    waitForMessageQueueEmpty(wait);

                // invoke selector
                Object res = proceed.invoke(self, args);

                if(attach)
                {
                    // before result is handed off, add critical waiting logic to fluent function chain
                    FluentWebElement fel = ((FluentWebElement)res)
                            .within(timeoutPeriod)
                            .ifInvisibleWaitUpTo(timeoutPeriod);
                    WebElement el = fel.getWebElement();

                    if(delegate.getClass().isAssignableFrom(JavascriptExecutor.class)) {
                        // make sure object is in current view, otherwise various browsers/versions don't like interaction
                        ((JavascriptExecutor) delegate).executeScript("arguments[0].scrollIntoView(true);", el);
                    }
                    res = fel;
                }
                return res;
            }
        };

        try { // create proxy object with method injected and return
            return (FluentWebDriver) factory.create(new Class[]{ WebDriver.class }, new Object[] { delegate }, handler);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }
        return null;
    }

    // method to wait for all unfinished DOM business
    static void waitForMessageQueueEmpty(WebDriverWait wait) {
        wait.until(documentReadyStateComplete());
        wait.until(jQueryAJAXCallsHaveCompleted());
        wait.until(angularPendingRequestsZero());
    }

    public static ExpectedCondition<Boolean> documentReadyStateComplete() {
        return new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver driver) {
                if(!driver.getClass().isAssignableFrom(JavascriptExecutor.class)) return true;
                JavascriptExecutor jse = (JavascriptExecutor)driver;
                String readyState = String.format("%s", jse.executeScript("return document ? document.readyState : null;"));
                System.out.println("Ready State => " + readyState);
                return readyState.equals("complete");
            }
        };
    }

    public static ExpectedCondition<Boolean> jQueryAJAXCallsHaveCompleted() {
        // verify that, if jQuery, all async actions have been completed
        return new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver driver) {
                if(!driver.getClass().isAssignableFrom(JavascriptExecutor.class)) return true;
                return (Boolean) ((JavascriptExecutor) driver).executeScript("return (window.jQuery ? (window.jQuery != null) && (jQuery.active === 0) : true);");
            }
        };
    }

    public static ExpectedCondition<Boolean> angularPendingRequestsZero() {
        // verify that, if Angular, all async actions have been completed
        return new ExpectedCondition<Boolean>() {
            @Nullable
            @Override
            public Boolean apply(@Nullable WebDriver driver) {
                if(!driver.getClass().isAssignableFrom(JavascriptExecutor.class)) return true;
                return (Boolean) ((JavascriptExecutor) driver).executeScript("try { return (angular && (typeof angular === 'object' || typeof angular === 'function') ? (angular.element(document).injector().get('$http').pendingRequests.length === 0) : true); } catch(e) { console.log(e); return true; }");
            }
        };
    }
}
