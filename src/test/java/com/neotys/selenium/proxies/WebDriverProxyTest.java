/*
 * Copyright (c) 2016, Neotys
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Neotys nor the names of its contributors may be
 *       used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NEOTYS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.neotys.selenium.proxies;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.interactions.Actions;

import static org.junit.Assert.assertNotNull;

/**
 * @author aaron
 *
 */
public class WebDriverProxyTest {

    /**
     * Test method for {@link com.neotys.selenium.proxies.WebDriverProxy#newInstance(org.openqa.selenium.WebDriver)}.
     */
    @Test
    public void testNewInstance() {
        final WebDriver webDriver = new HtmlUnitDriver();
        try {
            final NLWebDriver driver = WebDriverProxy.newInstance(webDriver);
            assertNotNull(driver);
        } finally {
            webDriver.close();
        }
    }

    /**
     * Test method for {@link com.neotys.selenium.proxies.NLWebDriver#getRegexToCleanURLs()}.
     */
    @Test
    public void testGetRegexToCleanURLs() {
        final WebDriver webDriver = new HtmlUnitDriver();
        try {
            final NLWebDriver driver = WebDriverProxy.newInstance(webDriver);
            final String regexToCleanURLs = driver.getRegexToCleanURLs();
            assertNotNull(regexToCleanURLs);
        } finally {
            webDriver.close();
        }
    }


    @Test
    public void testStartTransaction() {
        final WebDriver webDriver = new HtmlUnitDriver();
        try {
            final NLWebDriver driver = WebDriverProxy.newInstance(webDriver);
            // this should not throw an exception.
            driver.startTransaction("name");
        } finally {
            webDriver.close();
        }
    }

    @Test
    public void testStopTransaction() {
        final WebDriver webDriver = new HtmlUnitDriver();
        try {
            final NLWebDriver driver = WebDriverProxy.newInstance(webDriver);
            // this should not throw an exception.
            driver.stopTransaction();
        } finally {
            webDriver.close();
        }
    }

    /**
     * Test method for {@link com.neotys.selenium.proxies.WebDriverProxy#newInstance(org.openqa.selenium.WebDriver)}.
     * Assure that HasInputDevices and other random interfaces are where they need to be.
     */
    @Test
    public void testHasInputDevices() {
        final WebDriver webDriver = new HtmlUnitDriver();
        try {
            final NLWebDriver driver = WebDriverProxy.newInstance(webDriver);
            final Actions builder = new Actions(driver);
            builder.moveToElement(driver.findElement(By.xpath("//*"))).build().perform();
            assertNotNull(builder);
        } finally {
            webDriver.close();
        }
    }

}
