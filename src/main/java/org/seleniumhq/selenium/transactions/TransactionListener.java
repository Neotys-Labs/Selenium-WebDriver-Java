package org.seleniumhq.selenium.transactions;

/**
 * Created by paul on 11/6/17.
 */

public interface TransactionListener {
    void transactionStarted(WebDriverTransaction transaction);
    void transactionFinished(WebDriverTransaction transaction);
}
