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

import com.neotys.selenium.proxies.helpers.ProxySendHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;
import javassist.util.proxy.MethodHandler;
import org.openqa.selenium.WebDriver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by anouvel on 03/11/2016.
 */
public class MethodHandlers {

	private static final List<String> METHODS_SEND_ON_EXCEPTION_ONLY = Arrays.asList("findElements", "findElement");

	private static final List<String> METHODS_ALWAYS_SEND = Collections.singletonList("get");

	private static final List<String> METHODS_SET_LAST_ACTION = Arrays.asList("findElements", "findElement");

	static MethodHandler newEueMethodHandler(final SeleniumProxyConfig proxyConfig, final WebDriver webDriver) {
		return new MethodHandler() {
			@Override
			public Object invoke(final Object proxy, final Method method, Method proceed, final Object[] args) throws Throwable {
				try {
					if (!SeleniumProxyConfig.isEnabled()) {
						// if we're calling a method on our wrapper then handle it.
						if (isProxyableMethod(method)) {
							return method.invoke(proxyConfig, args);
						}
						return method.invoke(webDriver, args);
					}
					return new ProxySendHelper(proxyConfig, webDriver).sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY,
							METHODS_SET_LAST_ACTION, webDriver, webDriver, method, args);
				} catch (final InvocationTargetException e) {
					throw e.getCause();
				}
			}
		};
	}

	private static boolean isProxyableMethod(Method method) {
		return method.getDeclaringClass().isAssignableFrom(CustomProxyConfig.class)
				|| method.getDeclaringClass().isAssignableFrom(TransactionModifier.class)
				|| (method.getDeclaringClass().isAssignableFrom(ScopableTransactor.class) && method.getName().equals("getTransactionNames"));
	}

	static MethodHandler newDesignMethodHandler(final SeleniumProxyConfig proxyConfig, final WebDriver webDriver, final DesignManager designManager) {
		return new MethodHandler() {
			@Override
			public Object invoke(final Object proxy, final Method method, Method proceed, final Object[] args) throws Throwable {
				try {
					if (isProxyableMethod(method)) {
						return method.invoke(proxyConfig, args);
					}
					return method.invoke(webDriver, args);
				} catch (final InvocationTargetException e) {
					throw e.getCause();
				} finally {
					if ("quit".equals(method.getName())) {
						// stop the recording.
						designManager.stop();
					}
				}
			}
		};
	}

	static MethodHandler newNoApiMethodHandler(final SeleniumProxyConfig proxyConfig, final WebDriver webDriver){
		return new MethodHandler() {
			@Override
			public Object invoke(final Object proxy, final Method method, Method proceed, final Object[] args) throws Throwable {
				if (method.getDeclaringClass().isAssignableFrom(TransactionModifier.class)) {
					// nothing to do.
					return null;
				}

				try {
					// if we're calling a method on our wrapper then handle it.
					if (method.getDeclaringClass().isAssignableFrom(CustomProxyConfig.class)) {
						return method.invoke(proxyConfig, args);
					}
					return method.invoke(webDriver, args);
				} catch (final InvocationTargetException e) {
					throw e.getCause();
				}
			}
		};
	}
}
