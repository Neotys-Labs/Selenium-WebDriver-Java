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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.internal.Coordinates;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;

import com.neotys.selenium.proxies.helpers.ProxySendHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;
import com.neotys.selenium.proxies.helpers.WrapperUtils;

/** We tried to avoid using direct wrapper methods like this, but we found a case where an "instanceof" check was done
 * for a @RemoteWebElement in @org.openqa.selenium.remote.internal.WebElementToJsonConverter#apply(). */
public class RemoteWebElementWrapper extends RemoteWebElement {

	final WebDriver webDriver;
	final SeleniumProxyConfig proxyConfig;
	final ProxySendHelper proxySendHelper;
	final WrapperUtils wrapperUtils;
	final RemoteWebElement original;

	private static final List<String> METHODS_SEND_ON_EXCEPTION_ONLY = WebElementProxy.METHODS_SEND_ON_EXCEPTION_ONLY;

	private static final List<String> METHODS_ALWAYS_SEND = WebElementProxy.METHODS_ALWAYS_SEND;

	public RemoteWebElementWrapper(final WebDriver webDriver, final RemoteWebElement original, final SeleniumProxyConfig proxyConfig){
		this.webDriver = webDriver;
		this.proxyConfig = proxyConfig;
		this.proxySendHelper = new ProxySendHelper(proxyConfig);
		this.wrapperUtils = new WrapperUtils(proxyConfig);
		this.original = original;
	}
	
	@Override
	public void setFileDetector(FileDetector detector) {
		original.setFileDetector(detector);
	}
	
	@Override
	public void setId(String id) {
		original.setId(id);
	}
	
	@Override
	public void setParent(RemoteWebDriver parent) {
		super.setParent(parent);
		original.setParent(parent);
	}
	
	@Override
	public void click() {
		if (!SeleniumProxyConfig.isEnabled()) {
			original.click();
			return;
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("click", (Class<?>[])null);

			proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, (Object[])null);
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}
	}

	@Override
	public void submit() {
		if (!SeleniumProxyConfig.isEnabled()) {
			original.submit();
			return;
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("submit", (Class<?>[])null);

			proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, (Object[])null);
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}
	}

	@Override
	public String getTagName() {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.getTagName();
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("getTagName", (Class<?>[])null);

			return (String) proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, (Object[])null);
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}
	}

	@Override
	public String getAttribute(final String name) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.getAttribute(name);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("getAttribute", (new Class<?>[]{String.class}));

			return (String) proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{name});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElements(final By by) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElements(by);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElements", (new Class<?>[]{By.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{by});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElement(final By by) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElement(by);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElement", (new Class<?>[]{By.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{by});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementById(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementById(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementById", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsById(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsById(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsById", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByLinkText(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByLinkText(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByLinkText", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByLinkText(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByLinkText(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByLinkText", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByName(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByName(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByName", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByName(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByName(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByName", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByClassName(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByClassName(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByClassName", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByClassName(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByClassName(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByClassName", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByCssSelector(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByCssSelector(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByCssSelector", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByCssSelector(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByCssSelector(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByCssSelector", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByXPath(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByXPath(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByXPath", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByXPath(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByXPath(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByXPath", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByPartialLinkText(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByPartialLinkText(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByPartialLinkText", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByPartialLinkText(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByPartialLinkText(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByPartialLinkText", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebElement findElementByTagName(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementByTagName(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementByTagName", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public List<WebElement> findElementsByTagName(final String using) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.findElementsByTagName(using);
		}

		try {
			final Method method = RemoteWebElement.class.getDeclaredMethod("findElementsByTagName", (new Class<?>[]{String.class}));

			return proxySendHelper.sendAndReturn(METHODS_ALWAYS_SEND, METHODS_SEND_ON_EXCEPTION_ONLY, Collections.<String>emptyList(),
					webDriver, original, method, new Object[]{using});
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new NeotysWrappingException("Issue with NeoLoad proxy.", e);
		}	
	}

	@Override
	public WebDriver getWrappedDriver() {
		if (!SeleniumProxyConfig.isEnabled()) {
			return original.getWrappedDriver();
		}

		return (WebDriver) wrapperUtils.wrapIfNecessary(webDriver, super.getWrappedDriver());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		if (!SeleniumProxyConfig.isEnabled()) {
			return super.clone();
		}

		return wrapperUtils.wrapIfNecessary(webDriver, super.clone());
	}

	// automatically generated wrapper methods --------------------------------------------------------
	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getId()
	 */
	@Override
	public String getId() {
		return original.getId();
	}

	/**
	 * @param keysToSend
	 * @see org.openqa.selenium.remote.RemoteWebElement#sendKeys(java.lang.CharSequence[])
	 */
	@Override
	public void sendKeys(CharSequence... keysToSend) {
		original.sendKeys(keysToSend);
	}

	/**
	 * 
	 * @see org.openqa.selenium.remote.RemoteWebElement#clear()
	 */
	@Override
	public void clear() {
		original.clear();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#isSelected()
	 */
	@Override
	public boolean isSelected() {
		return original.isSelected();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return original.isEnabled();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getText()
	 */
	@Override
	public String getText() {
		return original.getText();
	}

	/**
	 * @param propertyName
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getCssValue(java.lang.String)
	 */
	@Override
	public String getCssValue(String propertyName) {
		return original.getCssValue(propertyName);
	}

	/**
	 * @param obj
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		return original.equals(obj);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#hashCode()
	 */
	@Override
	public int hashCode() {
		return original.hashCode();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#isDisplayed()
	 */
	@Override
	public boolean isDisplayed() {
		return original.isDisplayed();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getLocation()
	 */
	@Override
	public Point getLocation() {
		return original.getLocation();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getSize()
	 */
	@Override
	public Dimension getSize() {
		return original.getSize();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getRect()
	 */
	@Override
	public Rectangle getRect() {
		return original.getRect();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#getCoordinates()
	 */
	@Override
	public Coordinates getCoordinates() {
		return original.getCoordinates();
	}

	/**
	 * @param outputType
	 * @return
	 * @throws WebDriverException
	 * @see org.openqa.selenium.remote.RemoteWebElement#getScreenshotAs(org.openqa.selenium.OutputType)
	 */
	@Override
	public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
		return original.getScreenshotAs(outputType);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebElement#toString()
	 */
	@Override
	public String toString() {
		return original.toString();
	}
}
