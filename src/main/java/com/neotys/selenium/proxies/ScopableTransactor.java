package com.neotys.selenium.proxies;

/**
 * Created by paul on 11/7/17.
 */
public interface ScopableTransactor {
    void startTransaction(final String name, final RunnableThrowsExceptions fSteps) throws Exception;
    String[] getTransactionNames();
}
