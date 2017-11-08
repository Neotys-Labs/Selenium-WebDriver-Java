package com.neotys.selenium.proxies;

import org.openqa.selenium.WebDriver;
import org.seleniumhq.selenium.fluent.FluentMatcher;
import org.seleniumhq.selenium.fluent.FluentWebDriver;
import org.seleniumhq.selenium.fluent.FluentWebElement;
import org.seleniumhq.selenium.transactions.TransactableWebDriver;
import org.seleniumhq.selenium.transactions.TransactionListener;
import org.seleniumhq.selenium.transactions.WebDriverTransaction;

/**
 * Created by paul on 11/7/17.
 */
public class NLTransactableWebDriver extends TransactableWebDriver implements NLWebDriver, TransactionListener {

    private NLWebDriver delegate;

    public NLTransactableWebDriver(final NLWebDriver delegate) {
        super(delegate);
        this.delegate = delegate;
        super.addTransactionListener(this);
    }

    @Override
    public void setCustomName(String name) {
        delegate.setCustomName(name);
    }

    @Override
    public String getRegexToCleanURLs() {
        return delegate.getRegexToCleanURLs();
    }

    @Override
    public void startTransaction(String name) {
        super.startTransaction(name);
    }

    @Override
    public void stopTransaction() {
        delegate.stopTransaction();
    }

    @Override
    public void transactionStarted(WebDriverTransaction transaction) {
        delegate.startTransaction(transaction.getName());
    }

    @Override
    public void transactionFinished(WebDriverTransaction transaction) {
        this.stopTransaction();
    }

    @Override
    public void startTransaction(String name, RunnableThrowsExceptions fSteps) throws Exception {
        this.startTransaction(name, fSteps);
    }

    @Override
    public String[] getTransactionNames() {
        return delegate.getTransactionNames();
    }

    private FluentWebDriver fluent = null;

    @Override
    public FluentWebDriver fluent() {
        if(fluent == null)
            fluent = FluencyFactory.createFluentWebDriver(this, 30);
        return fluent;
    }

    @Override
    public FluentMatcher textContains(final String textToMatch) {
        return new FluentMatcher() {
            @Override
            public boolean matches(FluentWebElement fluentWebElement, int i) {
                return fluentWebElement.getText().toString().contains(textToMatch);
            }
        };
    }

}
