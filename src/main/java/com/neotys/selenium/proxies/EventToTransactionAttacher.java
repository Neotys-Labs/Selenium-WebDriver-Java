package com.neotys.selenium.proxies;

/**
 * Created by PSB on 11/6/17.
 */
public interface EventToTransactionAttacher {
    void registerForTransactions(NLWebDriver driver);
}
