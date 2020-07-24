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

import com.neotys.selenium.proxies.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.TargetLocator;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.*;

/**
 * @author ajohnson
 *
 */
public class WrapperUtils {

    /** Config settings. */
    private final SeleniumProxyConfig proxyConfig;

    /** Constructor.
     * @param proxyConfig
     */
    public WrapperUtils(final SeleniumProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    /**
     * @param webDriver
     * @param instanceToWrap
     * @return any special instances that need to be wrapped
     */
    @SuppressWarnings("unchecked")
    public <T> T wrapIfNecessary(final WebDriver webDriver, final Object instanceToWrap) {
        if (instanceToWrap instanceof Mouse) {
            return (T)new MouseProxy(webDriver, (Mouse) instanceToWrap, proxyConfig).getProxy();

        } else if (instanceToWrap instanceof Keyboard) {
            return (T)new KeyboardProxy(webDriver, (Keyboard) instanceToWrap, proxyConfig).getProxy();

        } else if (instanceToWrap instanceof Navigation) {
            return (T)new NavigationProxy(webDriver, (Navigation) instanceToWrap, proxyConfig).getProxy();

        } else if (instanceToWrap instanceof TargetLocator) {
            return (T)new TargetLocatorProxy(webDriver, (TargetLocator) instanceToWrap, proxyConfig).getProxy();

        } else if (instanceToWrap instanceof WebDriver) {
            return (T)WebDriverProxy.newEueInstance(webDriver);

        } else if (instanceToWrap instanceof RemoteWebElement) {
        	return (T)new RemoteWebElementWrapper(webDriver, (RemoteWebElement) instanceToWrap, proxyConfig);

        } else if (instanceToWrap instanceof WebElement) {
            return (T)new WebElementProxy(webDriver, (WebElement) instanceToWrap, proxyConfig).getProxy();

        } else if (instanceToWrap instanceof Options) {
            return (T)new OptionsProxy(webDriver, (Options) instanceToWrap, proxyConfig).getProxy();
            
        } else if (instanceToWrap instanceof List) {
            List<Object> newInstance;
            try {
                newInstance = (List<Object>) instanceToWrap.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                // the only known return type of list at the time of this writing is an ArrayList
                newInstance = new ArrayList<>();
            }

            for (final Object obj: (List<?>)instanceToWrap) {
                final Object wrappedObject = wrapIfNecessary(webDriver, obj);
                newInstance.add(wrappedObject);
            }

            return (T)newInstance;
        }
        
        return (T)instanceToWrap;
    }

	/** Ignore exceptions when getting the current URL. For use with Selendroid.
	 * @param webDriver
	 * @return
	 */
	public static String getURL(final WebDriver webDriver) {
		String url = "";
		try {
			url = webDriver.getCurrentUrl();
		} catch (final Exception e) {
			// ignored
		}

		return url;
	}

	/** Ignore exceptions when getting the current page title. For use with Selendroid.
	 * @param webDriver
	 * @return
	 */
	public static String getTitle(final WebDriver webDriver) {
		String title = "";
		try {
			title = webDriver.getTitle();
		} catch (final Exception e) {
			// ignored
		}

		return title;
	}
    
    /**
     * @param concreteClasses
     * @return all interfaces used by the class and its superclasses
     */
    public static Class<?>[] getInterfacesUsed(final Class<?> ... concreteClasses) {
        final Set<Class<?>> interfacesToUse = new LinkedHashSet<>();

        // look at the class and all superclasses for interfaces.
        for (final Class<?> classIterator: concreteClasses) {
            Class<?> currentClass = classIterator;
            do {
                if (classIterator.isInterface()) {
                    interfacesToUse.add(classIterator);
                }
                
                final Class<?>[] interfaces = currentClass.getInterfaces();
                if (interfaces != null) {
                    interfacesToUse.addAll(Arrays.asList(interfaces));
                }
                
                currentClass = currentClass.getSuperclass();
            } while (currentClass != null && currentClass != Object.class);
        }
        
        return interfacesToUse.toArray(new Class<?>[0]);
    }
}
