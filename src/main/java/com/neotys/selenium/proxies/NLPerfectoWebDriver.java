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

import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Keyboard;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.remote.CommandExecutor;
import org.openqa.selenium.remote.ErrorHandler;
import org.openqa.selenium.remote.FileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import com.neotys.selenium.proxies.helpers.WrapperUtils;

public class NLPerfectoWebDriver extends NLRemoteWebDriver{
	
	private final RemoteWebDriver remoteWebDriver;
	private final NLWebDriver webDriver;
	private final WrapperUtils wrapperUtils;
	
	public NLPerfectoWebDriver(final RemoteWebDriver originalWebDriver, final NLWebDriver webDriver,
							   final WrapperUtils wrapperUtils) {
		this.remoteWebDriver = originalWebDriver;
		this.webDriver = webDriver;
		this.wrapperUtils = wrapperUtils;
	}
	
	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return webDriver.hashCode();
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return webDriver.equals(obj);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getW3CStandardComplianceLevel()
	 */
	public int getW3CStandardComplianceLevel() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getW3CStandardComplianceLevel());
	}

	/**
	 * @param detector
	 * @see org.openqa.selenium.remote.RemoteWebDriver#setFileDetector(org.openqa.selenium.remote.FileDetector)
	 */
	public void setFileDetector(FileDetector detector) {
		remoteWebDriver.setFileDetector(detector);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getSessionId()
	 */
	public SessionId getSessionId() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getSessionId());
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getErrorHandler()
	 */
	public ErrorHandler getErrorHandler() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getErrorHandler());
	}

	/**
	 * @param handler
	 * @see org.openqa.selenium.remote.RemoteWebDriver#setErrorHandler(org.openqa.selenium.remote.ErrorHandler)
	 */
	public void setErrorHandler(ErrorHandler handler) {
		remoteWebDriver.setErrorHandler(handler);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getCommandExecutor()
	 */
	public CommandExecutor getCommandExecutor() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getCommandExecutor());
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getCapabilities()
	 */
	public Capabilities getCapabilities() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getCapabilities());
	}

	/**
	 * @param url
	 * @see org.openqa.selenium.remote.RemoteWebDriver#get(java.lang.String)
	 */
	public void get(String url) {
		webDriver.get(url);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getTitle()
	 */
	public String getTitle() {
		return webDriver.getTitle();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getCurrentUrl()
	 */
	public String getCurrentUrl() {
		return webDriver.getCurrentUrl();
	}

	/**
	 * @param outputType
	 * @return
	 * @throws WebDriverException
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getScreenshotAs(org.openqa.selenium.OutputType)
	 */
	public <X> X getScreenshotAs(OutputType<X> outputType) throws WebDriverException {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getScreenshotAs(outputType));
	}

	/**
	 * @param by
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElements(org.openqa.selenium.By)
	 */
	public List<WebElement> findElements(By by) {
		return webDriver.findElements(by);
	}

	/**
	 * @param by
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElement(org.openqa.selenium.By)
	 */
	public WebElement findElement(By by) {
		return webDriver.findElement(by);
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementById(java.lang.String)
	 */
	public WebElement findElementById(String using) {
		return webDriver.findElement(By.id(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsById(java.lang.String)
	 */
	public List<WebElement> findElementsById(String using) {
		return webDriver.findElements(By.id(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByLinkText(java.lang.String)
	 */
	public WebElement findElementByLinkText(String using) {
		return webDriver.findElement(By.linkText(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByLinkText(java.lang.String)
	 */
	public List<WebElement> findElementsByLinkText(String using) {
		return webDriver.findElements(By.linkText(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByPartialLinkText(java.lang.String)
	 */
	public WebElement findElementByPartialLinkText(String using) {
		return webDriver.findElement(By.partialLinkText(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByPartialLinkText(java.lang.String)
	 */
	public List<WebElement> findElementsByPartialLinkText(String using) {
		return webDriver.findElements(By.partialLinkText(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByTagName(java.lang.String)
	 */
	public WebElement findElementByTagName(String using) {
		return webDriver.findElement(By.tagName(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByTagName(java.lang.String)
	 */
	public List<WebElement> findElementsByTagName(String using) {
		return webDriver.findElements(By.tagName(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByName(java.lang.String)
	 */
	public WebElement findElementByName(String using) {
		return webDriver.findElement(By.name(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByName(java.lang.String)
	 */
	public List<WebElement> findElementsByName(String using) {
		return webDriver.findElements(By.name(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByClassName(java.lang.String)
	 */
	public WebElement findElementByClassName(String using) {
		return webDriver.findElement(By.className(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByClassName(java.lang.String)
	 */
	public List<WebElement> findElementsByClassName(String using) {
		return webDriver.findElements(By.className(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByCssSelector(java.lang.String)
	 */
	public WebElement findElementByCssSelector(String using) {
		return webDriver.findElement(By.cssSelector(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByCssSelector(java.lang.String)
	 */
	public List<WebElement> findElementsByCssSelector(String using) {
		return webDriver.findElements(By.cssSelector(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementByXPath(java.lang.String)
	 */
	public WebElement findElementByXPath(String using) {
		return webDriver.findElement(By.xpath(using));
	}

	/**
	 * @param using
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#findElementsByXPath(java.lang.String)
	 */
	public List<WebElement> findElementsByXPath(String using) {
		return webDriver.findElements(By.xpath(using));
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getPageSource()
	 */
	public String getPageSource() {
		return webDriver.getPageSource();
	}

	/**
	 * 
	 * @see org.openqa.selenium.remote.RemoteWebDriver#close()
	 */
	public void close() {
		webDriver.close();
	}

	/**
	 * 
	 * @see org.openqa.selenium.remote.RemoteWebDriver#quit()
	 */
	public void quit() {
		webDriver.quit();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getWindowHandles()
	 */
	public Set<String> getWindowHandles() {
		return webDriver.getWindowHandles();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getWindowHandle()
	 */
	public String getWindowHandle() {
		return webDriver.getWindowHandle();
	}

	/**
	 * @param script
	 * @param args
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#executeScript(java.lang.String, java.lang.Object[])
	 */
	public Object executeScript(String script, Object... args) {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.executeScript(script, args));
	}

	/**
	 * @param script
	 * @param args
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#executeAsyncScript(java.lang.String, java.lang.Object[])
	 */
	public Object executeAsyncScript(String script, Object... args) {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.executeAsyncScript(script, args));
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#switchTo()
	 */
	public TargetLocator switchTo() {
		return webDriver.switchTo();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#navigate()
	 */
	public Navigation navigate() {
		return webDriver.navigate();
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#manage()
	 */
	public Options manage() {
		return webDriver.manage();
	}

	/**
	 * @param level
	 * @see org.openqa.selenium.remote.RemoteWebDriver#setLogLevel(java.util.logging.Level)
	 */
	public void setLogLevel(Level level) {
		remoteWebDriver.setLogLevel(level);
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getKeyboard()
	 */
	public Keyboard getKeyboard() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getKeyboard());
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getMouse()
	 */
	public Mouse getMouse() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getMouse());
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#getFileDetector()
	 */
	public FileDetector getFileDetector() {
		return wrapperUtils.wrapIfNecessary(webDriver, remoteWebDriver.getFileDetector());
	}

	/**
	 * @return
	 * @see org.openqa.selenium.remote.RemoteWebDriver#toString()
	 */
	public String toString() {
		return webDriver.toString();
	}

	@Override
	public void startTransaction(final String name) {
		webDriver.startTransaction(name);
	}

	@Override
	public void stopTransaction() {
		webDriver.stopTransaction();
	}

	@Override
	public void setCustomName(final String name) {
		webDriver.setCustomName(name);
	}

	@Override
	public String getRegexToCleanURLs() {
		return webDriver.getRegexToCleanURLs();
	}
}
