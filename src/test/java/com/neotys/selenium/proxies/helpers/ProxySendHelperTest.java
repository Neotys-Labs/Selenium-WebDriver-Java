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
package com.neotys.selenium.proxies.helpers;

import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * @author aaron
 *
 */
public class ProxySendHelperTest {

    /**
     * Test method for {@link com.neotys.selenium.proxies.helpers.ProxySendHelper#getAdvancedValues(org.openqa.selenium.WebDriver)}.
     */
//    @Test
    public void testGetAdvancedValuesFireFox() {
        final WebDriver firefoxDriver = new FirefoxDriver();
        
        final ProxySendHelper proxySendHelper = new ProxySendHelper(new SeleniumProxyConfig("any"), firefoxDriver);

        try {
            final Map<String, Long> advancedValuesFF = proxySendHelper.getAdvancedValues(firefoxDriver);
            assertTrue("There should be some values for the firefox driver,", advancedValuesFF.size() > 0);

            final Map<String, Long> advancedValuesFFRun2 = proxySendHelper.getAdvancedValues(firefoxDriver);
            assertTrue("There should be no values for the second run of the firefox driver because the URL hasn't changed.", advancedValuesFFRun2.size() == 0);
            
        } finally {
            firefoxDriver.quit();
        }
    }

    /**
     * Test method for {@link com.neotys.selenium.proxies.helpers.ProxySendHelper#getAdvancedValues(org.openqa.selenium.WebDriver)}.
     */
    @Test
    public void testGetAdvancedValuesHtmlUnit() {
        final WebDriver htmlUnitDriver = new HtmlUnitDriver();

        final ProxySendHelper proxySendHelper = new ProxySendHelper(new SeleniumProxyConfig("any"), htmlUnitDriver);
        
        try {
            final Map<String, Long> advancedValuesHU = proxySendHelper.getAdvancedValues(htmlUnitDriver);
            assertTrue("There should be no values for the html unit driver,", advancedValuesHU.size() == 0);
        } finally {
            htmlUnitDriver.quit();
        }
    }

}
