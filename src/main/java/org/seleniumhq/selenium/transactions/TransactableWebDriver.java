package org.seleniumhq.selenium.transactions;

import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.events.WebDriverEventListener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by paul on 11/6/17.
 * A reference example of hooking generic TransactionListener to specific WebDriverEventListener
 */
public class TransactableWebDriver extends EventFiringWebDriver implements WebDriverEventListener {

    private WebDriver delegate = null;
    private ArrayList<TransactionListener> listeners = new ArrayList<>();

    public TransactableWebDriver(WebDriver delegate) {
        super(delegate);
        this.delegate = delegate;
        this.register(this);
    }

    public void addTransactionListener(TransactionListener listener) {
        listeners.add(listener);
    }
    public void removeTransactionListener(TransactionListener listener) {
        listeners.remove(listener);
    }

    private boolean isManagedTransaction = false;
    private WebDriverTransaction lastTransaction = null;

    public void startTransaction(WebDriverTransaction transaction, Runnable fSteps) {
        isManagedTransaction = true;
        _startTransaction(transaction, fSteps);
    }
    private void _startTransaction(WebDriverTransaction transaction, Runnable fSteps) {

        if(lastTransaction != null && !isManagedTransaction)
            finalizeTransaction(); // opportunistic finalization

        // flip transaction over
        lastTransaction = transaction;

        for (TransactionListener listener : listeners) {
            listener.transactionStarted(transaction);
        }

        // [vis.1] start listener for next dom-ready render event, intercept to take document screenshot, and associate with transaction
        // [vis.2] perform steps if scoped with a lambda
        if(fSteps != null) {
            fSteps.run();
            finalizeTransaction(); // deterministic finalization
        }

    }
    public void startTransaction(WebDriverTransaction transaction) {
        startTransaction(transaction, null);
    }
    public void startTransaction(String transactionName) { startTransaction((new WebDriverTransaction(transactionName)), null); }
    public void startTransaction(String transactionName, Runnable fSteps) { startTransaction((new WebDriverTransaction(transactionName)), fSteps); }

    private void finalizeTransaction() {
        if(lastTransaction != null) {

            // 1.
            // [vis.3] take a new document screenshot of final transaction state, diff the two screenshots to highlight changes, and update transaction

            WebDriverTransaction lt = lastTransaction;
            lastTransaction = null; // manage internal state first
            for (TransactionListener listener : listeners) {
                listener.transactionFinished(lt);
            }
        }
        isManagedTransaction = false;
    }

    private void _beforeInternalEvent(String eventDescription) {
        if(isManagedTransaction) return;
        _startTransaction(new WebDriverTransaction(eventDescription), null);
    }

    @Override
    public void get(String url) {
        super.get(url);
    }

    @Override
    public String getCurrentUrl() {
        return super.getCurrentUrl();
    }

    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @Override
    public List<WebElement> findElements(By by) {
        return super.findElements(by);
    }

    @Override
    public WebElement findElement(By by) {
        return super.findElement(by);
    }

    @Override
    public String getPageSource() {
        return super.getPageSource();
    }

    @Override
    public void close() {
        finalizeTransaction();
        super.close();
    }

    @Override
    public void quit() {
        finalizeTransaction();
        super.quit();
    }

    @Override
    public Set<String> getWindowHandles() {
        return super.getWindowHandles();
    }

    @Override
    public String getWindowHandle() {
        return super.getWindowHandle();
    }

    @Override
    public TargetLocator switchTo() {
        _beforeInternalEvent("switchTo");
        return super.switchTo();
    }

    @Override
    public Navigation navigate() {
        _beforeInternalEvent("navigate");
        return super.navigate();
    }

    @Override
    public Options manage() {
        return super.manage();
    }


    @Override
    public void beforeAlertAccept(WebDriver webDriver) {

    }

    @Override
    public void afterAlertAccept(WebDriver webDriver) {

    }

    @Override
    public void afterAlertDismiss(WebDriver webDriver) {

    }

    @Override
    public void beforeAlertDismiss(WebDriver webDriver) {

    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {
        if(isAmidstInternalClick) return; // append traffic due to unmanaged click into prior container

        String path = url;
        try {
            URL u = new URL(url);
            path = u.getPath();
            if(!(path != null && path.length() > 0))
                path = u.getHost();
        } catch (MalformedURLException e) {
        }
        _beforeInternalEvent("Navigate To: " + path);
        // set a flag to wait around until page loads...
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        // look at page title and rewrite last transaction name
    }

    @Override
    public void beforeNavigateBack(WebDriver driver) {
        _beforeInternalEvent("Navigate Back");
    }

    @Override
    public void afterNavigateBack(WebDriver driver) {

    }

    @Override
    public void beforeNavigateForward(WebDriver driver) {
        _beforeInternalEvent("Navigate Forward");
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
    }

    @Override
    public void beforeNavigateRefresh(WebDriver webDriver) {

    }

    @Override
    public void afterNavigateRefresh(WebDriver webDriver) {

    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {

    }

    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {

    }

    private boolean isAmidstInternalClick = false;

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        if(!isManagedTransaction) isAmidstInternalClick = true;

        String identity = null;
        WebElement cur = element;
        for(int i=0; i<7 && cur != null; i++) {
            identity = cur.getAttribute("id");
            if (isNoString(identity)) identity = cur.getAttribute("name");
            if (isNoString(identity)) identity = cur.getAttribute("title");
            if (isNoString(identity)) identity = cur.getText();
            if (isNoString(identity)) identity = cur.getTagName();
            if (isNoString(identity))
                cur = cur.findElement(By.xpath("./.."));
            else
                break;
        }
        if (isNoString(identity)) identity = "obj:"+element.getLocation().toString();
        // eventually compute smartest xpath
        _beforeInternalEvent("Click: " + identity);
    }

    private static boolean isNoString(String s) {
        return !(s != null && !StringUtils.isEmpty(s) && !StringUtils.isBlank(s));
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        if(isAmidstInternalClick) isAmidstInternalClick = false;
    }

    @Override
    public void beforeChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {

    }

    @Override
    public void afterChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {

    }

    @Override
    public void beforeScript(String script, WebDriver driver) {

    }

    @Override
    public void afterScript(String script, WebDriver driver) {

    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {

    }
}
