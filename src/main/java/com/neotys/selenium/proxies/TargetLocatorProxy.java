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
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.TargetLocator;

import com.neotys.selenium.proxies.helpers.ProxySendHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;
import com.neotys.selenium.proxies.helpers.WrapperUtils;

/**
 * @author aaron
 *
 */
public class TargetLocatorProxy {

    /** The original object where we call methods. */
    private final TargetLocator original;

    /** Help us get info about the current page (title and url). */
    private final WebDriver webDriver;

    /** Config settings. */
    private final SeleniumProxyConfig proxyConfig;

    private static final List<String> METHODS_SEND_ON_EXCEPTION_ONLY = Arrays.asList(new String[]{"frame", "parentFrame", "window", "defaultContent",
            "activeElement", "alert"});

    private static final List<String> METHODS_ALWAYS_SEND = Arrays.asList(new String[]{});

    /** Constructor.
     * @param webDriver
     * @param targetLocator
     */
    public TargetLocatorProxy(final WebDriver webDriver, final TargetLocator targetLocator, final SeleniumProxyConfig proxyConfig) {
        this.webDriver = webDriver;
        this.original = targetLocator;
        this.proxyConfig = proxyConfig;
    }

    /** @return */
    public TargetLocator getProxy() {
        return (TargetLocator)Proxy.newProxyInstance(TargetLocator.class.getClassLoader(), WrapperUtils.getInterfacesUsed(original.getClass(), TargetLocator.class), invocationHandler);
    }

    /** Listens to calls on various methods. */
    private final InvocationHandler invocationHandler = new InvocationHandler() {
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            if (!SeleniumProxyConfig.isEnabled()) {
                return method.invoke(original, args);
            }

            return new ProxySendHelper(proxyConfig, webDriver).sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, 
            		Collections.<String> emptyList(), webDriver, original, method, args);
        }
    };
}
