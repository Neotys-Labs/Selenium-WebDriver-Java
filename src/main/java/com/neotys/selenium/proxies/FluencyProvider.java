package com.neotys.selenium.proxies;

import org.seleniumhq.selenium.fluent.FluentMatcher;
import org.seleniumhq.selenium.fluent.FluentWebDriver;

/**
 * Created by paul on 11/7/17.
 */
public interface FluencyProvider {
    FluentWebDriver fluent();
    FluentMatcher textContains(String textToMatch);
}
