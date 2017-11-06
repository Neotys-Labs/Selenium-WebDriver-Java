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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import org.openqa.selenium.WebDriver;

import com.neotys.selenium.proxies.helpers.ProxySendHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;
import com.neotys.selenium.proxies.helpers.WrapperUtils;
import com.thoughtworks.selenium.Selenium;

/**
 * @author aaron
 *
 */
public class WebDriverBackedSeleniumProxy {

    /** The original object where we call methods. */
    private final Selenium original;

    /** Config settings. */
    private final SeleniumProxyConfig proxyConfig;

    private static final List<String> METHODS_SEND_ON_EXCEPTION_ONLY = Arrays.asList(new String[]{"click", "doubleClick", "clickAt", "select", "selectWindow",
            "selectPopUp", "selectFrame", "contextMenu", "doubleClickAt", "contextMenuAt", "fireEvent", "focus", "keyPress", "keyDown", "keyUp", "mouseOver",
            "mouseOut", "mouseDown", "mouseDownRight", "mouseDownAt", "mouseDownRightAt", "mouseUp", "mouseUpRight", "mouseUpAt", "mouseUpRightAt",
            "mouseMove", "mouseMoveAt", "type", "typeKeys", "check", "uncheck", "addSelection", "removeSelection", "removeAllSelections", "submit", "openWindow",
            "getValue", "getText", "highlight", "isChecked", "getSelectedLabels", "getSelectedLabel", "getSelectedValues", "getSelectedValue", "getSelectedIndexes",
            "getSelectedIndex", "getSelectedIds", "getSelectedId", "isSomethingSelected", "getSelectOptions", "getAttribute", "dragdrop", "dragAndDrop",
            "dragAndDropToObject", "setCursorPosition", "getElementIndex", "assignId", "attachFile", "goBack", "refresh", "close", "getCookie", "getCookieByName",
            "isCookiePresent", "deleteCookie", "captureScreenshot", "captureScreenshotToString"});

    private static final List<String> METHODS_ALWAYS_SEND = Arrays.asList(new String[]{"waitForPopUp", "waitForCondition", "waitForPageToLoad",
            "waitForFrameToLoad", "open"});

    private static final List<String> METHODS_SET_LAST_ACTION = Arrays.asList(new String[]{"click", "doubleClick", "clickAt", "select", "selectWindow",
            "selectPopUp", "selectFrame"});

    /** Constructor.
     * @param selenium */
    public WebDriverBackedSeleniumProxy(final Selenium selenium) {
        this.original = selenium;
        this.proxyConfig = new SeleniumProxyConfig("");
    }

    /** Create a proxy that sends data to the Data Exchange server.
     * @param selenium
     * @return
     */
    public static NLSelenium newInstance(final Selenium selenium) {
        final WebDriverBackedSeleniumProxy driverProxy = new WebDriverBackedSeleniumProxy(selenium);
        
        return (NLSelenium)Proxy.newProxyInstance(WebDriver.class.getClassLoader(), WrapperUtils.getInterfacesUsed(selenium.getClass(), NLSelenium.class), driverProxy.invocationHandler);
    }

    /** Listens to calls on various methods. */
    private final InvocationHandler invocationHandler = new InvocationHandler() {
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (!SeleniumProxyConfig.isEnabled()) {
                // if we're calling a method on our wrapper then handle it.
                if (method.getDeclaringClass().isAssignableFrom(CustomProxyConfig.class)) {
                    return method.invoke(proxyConfig, args);
                }

                return method.invoke(original, args);
            }

            if ("open".equals(method.getName())) {
                proxyConfig.setLastAction("open url");
            }

            return new ProxySendHelper(proxyConfig, null).sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, 
            		METHODS_SET_LAST_ACTION, original, method, args);
        }
    };

}
