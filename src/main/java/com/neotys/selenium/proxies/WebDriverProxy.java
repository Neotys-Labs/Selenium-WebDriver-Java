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

import com.google.common.base.Optional;
import com.neotys.selenium.proxies.helpers.ModeHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;
import com.neotys.selenium.proxies.helpers.WrapperUtils;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.InvocationTargetException;

import static com.neotys.selenium.proxies.MethodHandlers.*;

/**
 * @author aaron
 *
 */
public class WebDriverProxy {

	/**
     * Deprecated since 2.0.0, NLWebDriverFactory should be used instead.
     */
    @Deprecated
	public static <T extends WebDriver> T newInstance(final WebDriver webDriver) {
        return newEueInstance(webDriver);
    }

    /**
     * Create a wrapper that sends data to the Data Exchange server.
     */
    public static <T extends WebDriver> T newEueInstance(final WebDriver webDriver) {
        return newEueInstance(webDriver, Optional.<String>absent());
    }

    /**
     * Create a wrapper that sends data to the Data Exchange server.
     */
    public static <T extends WebDriver> T newEueInstance(final WebDriver webDriver, final Optional<String> userPathName) {
        // we make sure right mode is selected when initializing with the deprecated method.
        System.setProperty(ModeHelper.OPT_SELENIUM_WRAPPER_MODE, ModeHelper.MODE_END_USER_EXPERIENCE);
        final SeleniumProxyConfig proxyConfig = newSeleniumProxyConfig(webDriver);
        initSeleniumConfigForEUE(webDriver, userPathName, proxyConfig);
        return newInstance(webDriver, proxyConfig, newEueMethodHandler(proxyConfig, webDriver));
    }

    /**
     * Execute the selenium script, no interaction with NeoLoad.
     */
    static <T extends WebDriver> T newNoAPIInstance(final WebDriver webDriver) {
        final SeleniumProxyConfig proxyConfig = newSeleniumProxyConfig(webDriver);
        return newInstance(webDriver, proxyConfig, newNoApiMethodHandler(proxyConfig, webDriver));
    }

	/**
     * To use during a NeoLoad record, to update a User Path.
     * @param webDriver the webDriver should use NeoLoad as Proxy
     * @param designManager
     */
    static <T extends WebDriver> T newDesignInstance(final WebDriver webDriver, final DesignManager designManager) {
        final SeleniumProxyConfig proxyConfig = newSeleniumProxyConfig(webDriver);
        return newInstance(webDriver, proxyConfig, newDesignMethodHandler(proxyConfig, webDriver, designManager));
    }

    private static SeleniumProxyConfig newSeleniumProxyConfig(final WebDriver webDriver) {
        return new SeleniumProxyConfig(webDriver.getClass().getSimpleName());
    }

    private static void initSeleniumConfigForEUE(final WebDriver webDriver, final Optional<String> userPathName, final SeleniumProxyConfig proxyConfig) {
        proxyConfig.setUserPathName(userPathName);
        if(webDriver instanceof HasCapabilities){
            final HasCapabilities hasCapabilities = (HasCapabilities) webDriver;
            proxyConfig.setCapabilities(Optional.fromNullable(hasCapabilities.getCapabilities()));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends WebDriver> T newInstance(final WebDriver webDriver, final SeleniumProxyConfig proxyConfig, final MethodHandler methodHandler){
        final ProxyFactory factory = new ProxyFactory();
        if(webDriver instanceof RemoteWebDriver){
            factory.setSuperclass(NLRemoteWebDriver.class);
        } else if (webDriver instanceof HasInputDevices) {
            factory.setInterfaces(new Class[]{NLHasInputDeviceWebDriver.class});
        }else {
            factory.setInterfaces(new Class[]{NLWebDriver.class});
        }
        final NLWebDriver nlWebDriver;
        try {
            nlWebDriver = (NLWebDriver) factory.create(new Class<?>[0], new Object[0], methodHandler);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        if (!proxyConfig.isUsingPerfecto(webDriver)) {
            return (T) nlWebDriver;
        }

        final WrapperUtils wrapperUtils = new WrapperUtils(proxyConfig);
        return (T) new NLPerfectoWebDriver((RemoteWebDriver) webDriver, nlWebDriver, wrapperUtils);
    }
}
