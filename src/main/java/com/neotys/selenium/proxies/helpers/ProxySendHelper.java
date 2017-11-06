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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.neotys.selenium.proxies.TransactionModifier;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.neotys.selenium.proxies.CustomProxyConfig;
import com.thoughtworks.selenium.Selenium;

/**
 * @author aaron
 *
 */
public class ProxySendHelper {

    /** Config settings. */
    private final SeleniumProxyConfig proxyConfig;

    /** Wrap returned types with our proxies */
    private final WrapperUtils wrapperUtils;
    
    /** Whether we use navigation timing or not. */
    private boolean navigationTimingActive = SeleniumProxyConfig.isNavigationTimingEnabled();
    
    /** Helps us decide whether to send navigation timing values or not. */
    private static String navigationTimingPreviousDomain = "";
    
    /** Helps us decide whether to send navigation timing values or not. */
    private static Map<String, Long> navigationTimingPreviousAdvancedValues = Collections.emptyMap();
    
    /** Parses and sends results to perfecto. */
    private PerfectoResultsHelper perfectoResultsHelper = null;
    
    /** Helps sends results to perfecto. */
    private final WebDriver webDriver;
    
    /** When the script begins. */
    private long actualScriptStartTime;

    /**
     * @param proxyConfig
     * @param webDriver 
     */
    public ProxySendHelper(final SeleniumProxyConfig proxyConfig, final WebDriver webDriver) {
        this.proxyConfig = proxyConfig;
        this.wrapperUtils = new WrapperUtils(proxyConfig);
        this.actualScriptStartTime = System.currentTimeMillis();
        this.webDriver = webDriver;
    }

    /**
     * @param methodsAlwaysSend a list of methods for which to send data
     * @param methodsSendOnExceptionOnly a list of methods for which to send data
     * @param methodsSetLastAction
     * @param webDriverBackedSelenium
     * @param method the method to invoke
     * @param args the args for the method
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public Object sendAndReturn(final Collection<String> methodsAlwaysSend, final Collection<String> methodsSendOnExceptionOnly,
            final Collection<String> methodsSetLastAction,
            final Selenium webDriverBackedSelenium, final Method method, final Object[] args)
            throws IllegalAccessException, InvocationTargetException {

        try {
        	// if we're calling a method on our wrapper then handle it.
            if (isInvokedInSeleniumProxyConfig(method)) {
                return method.invoke(proxyConfig, args);
            }
    
            // set the last action if necessary
            handleSetLastAction(methodsSetLastAction, method, args);
    
            // get the current url and title
            Object returnValue = null;
    
            // always start the timer and create an entry, even if we don't use it.
            final EntryHandler eh = EntryHandler.start(proxyConfig);
            try {
                returnValue = method.invoke(webDriverBackedSelenium, args);
    
            } catch (final Exception caughtException) {
                final Throwable cause = caughtException.getCause();
                
                if (cause instanceof RuntimeException) {
                    final RuntimeException rte = new RuntimeException(cause.getMessage() + " {\"method\":\"" + method.getName() + "\",\"params\":\"" + 
                            Arrays.asList(args) + "\"}", cause);
                    // if either list contains the method then we send the value.
                    if (methodsSendOnExceptionOnly.contains(method.getName()) || methodsAlwaysSend.contains(method.getName())) {
                        eh.sendEntryThrow(WrapperUtils.getURL(webDriverBackedSelenium), WrapperUtils.getTitle(webDriverBackedSelenium), rte, method.getName(),
                                Collections.<String, Long> emptyMap());
                    }
                }
                
                throw caughtException;
            }
            
            if (methodsAlwaysSend.contains(method.getName())) {
                eh.sendEntry(WrapperUtils.getURL(webDriverBackedSelenium), WrapperUtils.getTitle(webDriverBackedSelenium), method.getName(), 
                        Collections.<String, Long> emptyMap());
                return returnValue;
            }
    
            return returnValue;
        } catch (final RuntimeException e) {
        	proxyConfig.debug("Error from Selenium while calling method: " + method + " on class: " + webDriverBackedSelenium.getClass() + 
        			" (" + e.getMessage() + ")");
        	throw e;
        }
    }

    /**
     * @param methodsSetLastAction
     * @param method
     * @param args
     */
    private void handleSetLastAction(final Collection<String> methodsSetLastAction, final Method method, final Object[] args) {
        if (methodsSetLastAction.contains(method.getName())) {
            proxyConfig.setLastAction(method.getName() + " " + args[0]);
        }
    }

    /**
     * @param methodsAlwaysSend a list of methods for which to send data
     * @param methodsSendOnExceptionOnly a list of methods for which to send data
     * @param methodsSetLastAction
     * @param webDriver
     * @param original invoke the method on this object
     * @param method the method to invoke
     * @param args the args for the method
     * @return
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public <T> T sendAndReturn(final Collection<String> methodsAlwaysSend, final Collection<String> methodsSendOnExceptionOnly,
            final Collection<String> methodsSetLastAction,
            final WebDriver webDriver, final Object original, final Method method, final Object[] args)
            throws IllegalAccessException, InvocationTargetException {

    	handlePerfectoData(webDriver, method.getName(), "quit");

    	try {
            // if we're calling a method on our wrapper then handle it.
            if (isInvokedInSeleniumProxyConfig(method)) {
                return (T) method.invoke(proxyConfig, args);
            }
            
            // set the last action if necessary
            handleSetLastAction(methodsSetLastAction, method, args);
    
            // get the current url and title
            Object returnValue = null;
    
            // always start the timer and create an entry, even if we don't use it.
            final EntryHandler eh = EntryHandler.start(proxyConfig);
            
            try {
                final Object methodReturnValue = method.invoke(original, args);
				returnValue = wrapperUtils.wrapIfNecessary(webDriver, methodReturnValue);
    
            } catch (final Exception caughtException) {
                final Throwable cause = caughtException.getCause();
                
                if (cause instanceof RuntimeException) {
                    final RuntimeException rte = (RuntimeException)cause;
                    // if either list contains the method then we send the value.
                    if (methodsSendOnExceptionOnly.contains(method.getName()) || methodsAlwaysSend.contains(method.getName())) {
                        eh.sendEntryThrow(WrapperUtils.getURL(webDriver), WrapperUtils.getTitle(webDriver), rte, method.getName(), getAdvancedValues(webDriver));
                    }
                }
                
            	handlePerfectoData(webDriver, method.getName(), "close");
                throw caughtException;
            }
    
            if (methodsAlwaysSend.contains(method.getName())) {
                eh.sendEntry(WrapperUtils.getURL(webDriver), WrapperUtils.getTitle(webDriver), method.getName(), getAdvancedValues(webDriver));

            	handlePerfectoData(webDriver, method.getName(), "close");
                return (T) wrapperUtils.wrapIfNecessary(webDriver, returnValue);
            }

        	handlePerfectoData(webDriver, method.getName(), "close");
            return (T) returnValue;
        } catch (final RuntimeException e) {
        	proxyConfig.debug("Error from Selenium while calling method: " + method + " on class: " + original.getClass() + 
        			" (" + e.getMessage() + ")");
        	throw e;
        }
    }

    private static boolean isInvokedInSeleniumProxyConfig(final Method method) {
        return method.getDeclaringClass().isAssignableFrom(CustomProxyConfig.class) || method.getDeclaringClass().isAssignableFrom(TransactionModifier.class);
    }

    private void handlePerfectoData(final WebDriver webDriver, final String actualMethodName, final String methodToMatch) {
		if (methodToMatch.equals(actualMethodName)) {
			if (webDriver != null && proxyConfig.isUsingPerfecto(webDriver)) {
				if (perfectoResultsHelper == null) {
			        this.perfectoResultsHelper = new PerfectoResultsHelper(this, (RemoteWebDriver) webDriver, proxyConfig, actualScriptStartTime);
				}
				
				perfectoResultsHelper.sendResults();
			}
		}
	}
	
    /** Use the dom to get values. From http://www.w3.org/TR/navigation-timing/
     * @param webDriver
     * @return
     */
    synchronized Map<String, Long> getAdvancedValues(final WebDriver webDriver) {
        if (!navigationTimingActive || !(webDriver instanceof JavascriptExecutor)) {
            return Collections.emptyMap();
        }
        
        final Map<String, Long> advancedValues = new LinkedHashMap<>();
        
        try {
            final JavascriptExecutor javascriptExecutor = (JavascriptExecutor)webDriver;
            final Long redirectStart = (Long)javascriptExecutor.executeScript("return window.performance.timing.redirectStart");
            final Long fetchStart = (Long)javascriptExecutor.executeScript("return window.performance.timing.fetchStart");
            final Long responseStart = (Long)javascriptExecutor.executeScript("return window.performance.timing.responseStart");
            final Long domContentLoadedEventStart = (Long)javascriptExecutor.executeScript("return window.performance.timing.domContentLoadedEventStart");
            final Long domLoadEventStart = (Long)javascriptExecutor.executeScript("return window.performance.timing.loadEventStart");
            final Long domLoadEventEnd = (Long)javascriptExecutor.executeScript("return window.performance.timing.loadEventEnd");
    
            final long start = redirectStart == 0 ? fetchStart : redirectStart;
            
            final Long timeToFirstByte = safeSubtract(responseStart, start);
            final Long domContentLoaded = safeSubtract(domContentLoadedEventStart, start);
            final Long onLoad = safeSubtract(domLoadEventStart, start);
            final Long documentComplete = safeSubtract(domLoadEventEnd, start);
            
            // the key is the label that appears in NeoLoad. A slash (/) adds another path element.
            putIfNotNull(advancedValues, "Time To First Byte", timeToFirstByte);
            putIfNotNull(advancedValues, "DOM Content Loaded", domContentLoaded);
            putIfNotNull(advancedValues, "On Load", onLoad);
            putIfNotNull(advancedValues, "Document Complete", documentComplete);
            
        } catch (final Exception e) {
            SeleniumProxyConfig.debugMessage("Exception using navigating timing with " + webDriver.getClass().getName() + ": " + e.getMessage());
            
            // if anything ever goes wrong then give up.
            navigationTimingActive = false;
            return Collections.emptyMap();
        }

        // make sure we're not sending exactly the same values as last time.
        final String currentDomain = getDomainName(webDriver.getCurrentUrl());
        if (navigationTimingPreviousDomain.equals(currentDomain) && mapsAreEqual(navigationTimingPreviousAdvancedValues, advancedValues)) {
            // nothing has changed so don't send the same data over again.
            return Collections.emptyMap();
        }
        
        navigationTimingPreviousDomain = currentDomain;
        navigationTimingPreviousAdvancedValues = advancedValues;
        
        return advancedValues;
    }
    
    /**
     * @param map1
     * @param map2
     * @return
     */
    private static <K,V> boolean mapsAreEqual(final Map<K,V>map1, Map<K,V> map2) {
        if (map1.size() != map2.size()) {
           return false;
        }
        
        for (final Map.Entry<K, V> entry: map1.entrySet()) {
            if (!map2.containsKey(entry.getKey())) {
                return false;
            }
            
            final V map2Value = map2.get(entry.getKey());
            if (entry.getValue() == null && map2Value != null) {
                return false;
            }
            
            if (!entry.getValue().equals(map2Value)) {
                return false;
            }
        }
        
        return true;
     }

    /** Only add the value if it's not null.
     * @param map
     * @param key
     * @param value
     */
    private static void putIfNotNull(final Map<String, Long> map, final String key, final Long value) {
        if (value == null) {
            return;
        }
        
        map.put(key, value);
    }

    /** @param val1
     * @param val2
     * @return  val1 - val2 or null if either value is null.
     */
    private static final Long safeSubtract(final Long val1, final Long val2) {
        if (val1 == null || val2 == null) {
            return null;
        }
        
        return val1 - val2;
    }
    
    /** From http://stackoverflow.com/a/9608008
     * @param url
     * @return the domain name from a URL
     */
    private static String getDomainName(final String url) {
        final URI uri;
        try {
            uri = new URI(url);
            final String domain = uri.getHost() == null ? "" : uri.getHost();
            
            return domain.startsWith("www.") ? domain.substring(4) : domain;
            
        } catch (Exception e) {
            // don't care.
            return "";
        }
    }
}
