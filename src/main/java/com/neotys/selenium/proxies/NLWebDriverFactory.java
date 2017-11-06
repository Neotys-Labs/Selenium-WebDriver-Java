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
import com.neotys.rest.design.client.DesignAPIClient;
import com.neotys.rest.error.NeotysAPIException;
import com.neotys.selenium.proxies.helpers.ModeHelper;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

/**
 * Factory of NeoLoad Selenium WebDriver.
 * There are 3 modes:
 * <ul>
 * <li>NoApi (default)
 * <li>Design
 * <li>EndUserExperience.
 * </ul>
 * The nl.selenium.proxy.mode property should be used in order to choose mode. For example:
 * -Dnl.selenium.proxy.mode=Design
 * <p/>
 * When NoApi is chosen there is no interaction with NeoLoad.
 * When Design is chosen, a recording will be start on NeoLoad through Design API.
 * When EndUserExperience is chosen, selenium metrics will be send to NeoLoad through Data Exchange API.
 * <p/>
 * Created by anouvel on 08/11/2016.
 */
public class NLWebDriverFactory {

	private static final String DEFAULT_USER_PATH = "UserPath";

	private static String getDomainName(final String url) {
		URI uri;
		try {
			uri = new URI(url);
			String domain = uri.getHost();
			return domain.startsWith("www.") ? domain.substring(4) : domain;
		} catch (final URISyntaxException e) {
			e.printStackTrace();
			return "localhost";
		}
	}

	/**
	 * If the design mode is chosen then the driver will use NeoLoad as Proxy.
	 * @param capabilities capabilities modified ti use NeoLoad as Proxy.
	 */
	public static DesiredCapabilities addProxyCapabilitiesIfNecessary(final DesiredCapabilities capabilities) {
		if (!ModeHelper.Mode.DESIGN.equals(ModeHelper.getMode())) {
			return capabilities;
		}
		final DesignAPIClient designAPIClient = DesignManager.getDesignApiClient();

		// should be called after design API initialization.
		final String host = getDomainName(SeleniumProxyConfig.getDesignAPIURL());
		final int port;
		try {
			port = designAPIClient.getRecorderSettings().getProxySettings().getPort();
		} catch (IOException | GeneralSecurityException | URISyntaxException | NeotysAPIException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		final String proxyString = host + ":" + port;

		Proxy proxy = new Proxy();
		proxy.setProxyType(Proxy.ProxyType.MANUAL);
		proxy.setHttpProxy(proxyString);
		proxy.setSslProxy(proxyString);

		capabilities.setCapability(CapabilityType.PROXY, proxy);
		capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
		return capabilities;
	}

	/**
	 * Create a NeoLoad instance of the webDriver.
	 * In Design mode, the default User Path will be created or updated if it is already present in opened project.
	 *
	 * @param webDriver an instance of a <code>WebDriver</code> as <code>ChromeDriver</code> or <code>FirefoxDriver</code>.
	 */
	public static <T extends WebDriver> T newNLWebDriver(final WebDriver webDriver) {
		return newNLWebDriver(webDriver, Optional.<String>absent(), Optional.<String>absent(), new ParamBuilderProvider());
	}

	/**
	 * Create a NeoLoad instance of the webDriver.
	 * In Design mode, the userPath in parameter will be created or updated if it is already present in opened project.
	 * In EndUserExperience mode, the userPath in parameter will be added to the path of entries sent to NeoLoad.
	 *
	 * @param webDriver an instance of a <code>WebDriver</code> as <code>ChromeDriver</code> or <code>FirefoxDriver</code>.
	 * @param userPath  the name of the UserPath
	 */
	public static <T extends WebDriver> T newNLWebDriver(final WebDriver webDriver, final String userPath) {
		return newNLWebDriver(webDriver, Optional.fromNullable(userPath), Optional.<String>absent(), new ParamBuilderProvider());
	}

	/**
	 * Create a NeoLoad instance of the webDriver.
	 * In Design mode, project in parameter will be opened and the userPath in parameter will be created or updated if it is already present in opened project.
	 * In EndUserExperience mode, the userPath in parameter will be added to the path of entries sent to NeoLoad.
	 *
	 * @param webDriver an instance of a <code>WebDriver</code> as <code>ChromeDriver</code> or <code>FirefoxDriver</code>.
	 * @param userPath  the name of the UserPath
	 * @param projectPath the path of the project to open in NeoLoad.
	 */
	public static <T extends WebDriver> T newNLWebDriver(final WebDriver webDriver, final String userPath, final String projectPath) {
		return newNLWebDriver(webDriver, Optional.fromNullable(userPath), Optional.fromNullable(projectPath), new ParamBuilderProvider());
	}

	/**
	 * Create a NeoLoad instance of the webDriver.
	 * In Design mode, project in parameter will be opened and the userPath in parameter will be created or updated if it is already present in opened project.
	 * In EndUserExperience mode, the userPath in parameter will be added to the path of entries sent to NeoLoad.
	 *
	 * @param webDriver an instance of a <code>WebDriver</code> as <code>ChromeDriver</code> or <code>FirefoxDriver</code>.
	 * @param userPath  the name of the UserPath
	 * @param projectPath the path of the project to open in NeoLoad.
	 * @param paramBuilderProvider ParamBuilderProvider class can be overridden in order to update parameters.
	 */
	public static <T extends WebDriver> T newNLWebDriver(final WebDriver webDriver, final String userPath, final String projectPath,
														 final ParamBuilderProvider paramBuilderProvider) {
		return newNLWebDriver(webDriver, Optional.fromNullable(userPath), Optional.fromNullable(projectPath), paramBuilderProvider);
	}

	private static <T extends WebDriver> T newNLWebDriver(final WebDriver webDriver, final Optional<String> userPath, final Optional<String> projectPath,
														  final ParamBuilderProvider paramBuilderProvider) {
		final ModeHelper.Mode mode = ModeHelper.getMode();
		switch (mode) {
			case END_USER_EXPERIENCE:
				return WebDriverProxy.newEueInstance(webDriver, userPath);
			case DESIGN:
				final DesignManager designManager = new DesignManager(userPath.or(DEFAULT_USER_PATH), projectPath, paramBuilderProvider);
				designManager.start();
				return WebDriverProxy.newDesignInstance(webDriver, designManager);
			case NO_API:
			default:
				return WebDriverProxy.newNoAPIInstance(webDriver);
		}
	}

	private NLWebDriverFactory() {
	}
}
