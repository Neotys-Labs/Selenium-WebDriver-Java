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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.exception.ODataException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.model.Entry;
import com.neotys.rest.dataexchange.model.EntryBuilder;
import com.neotys.rest.dataexchange.model.TimerBuilder;
import com.neotys.rest.dataexchange.util.Statuses;
import com.neotys.rest.error.NeotysAPIException;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig.PathNamingPolicy;

public class EntryHandler {
	/** Where to send the data and how to collect it. */
	private final SeleniumProxyConfig proxyConfig;

	/** When the timer was started. */
	private final long startTime;

	/** Temporarily stores an exception if one was thrown. */
	private RuntimeException exception = null;

	private EntryHandler(final SeleniumProxyConfig delegateConfig) {
		this.proxyConfig = delegateConfig;
		this.startTime = System.currentTimeMillis();
	}

	/**
	 * @param proxyConfig
	 * @return
	 */
	public static EntryHandler start(final SeleniumProxyConfig proxyConfig) {
		return new EntryHandler(proxyConfig);
	}

	/** @param exception the exception to set */
	public void setException(final RuntimeException exception) {
		this.exception = exception;
	}

	/** Collects data, sends the data to NeoLoad, and throws the stored Exception if there is one.
	 * @param advancedValues key -> label, value -> value.
	 */
	public void sendEntry(final String currentURL, final String pageTitle, final String methodName, final Map<String, Long> advancedValues) {
		if (!SeleniumProxyConfig.isEnabled()) {
			return;
		}

		try {
			sendData(currentURL, pageTitle, methodName, advancedValues);
		} catch (GeneralSecurityException | IOException | URISyntaxException | NeotysAPIException | ODataException e) {
			throw new RuntimeException(e);
		}
		throwStoredException();
	}

	public void sendEntryThrow(final String currentURL, final String pageTitle, final RuntimeException e,
			final String methodName, final Map<String, Long> advancedValues) {
		exception = e;
		sendEntry(currentURL, pageTitle, methodName, advancedValues);
	}

	/** Send the data as an entry to NeoLoad.
	 * @param currentURL
	 * @param pageTitle
	 * @param methodName
	 * @param advancedValues
	 * @throws GeneralSecurityException
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws NeotysAPIException
	 * @throws ODataException
	 */
	void sendData(final String currentURL, final String pageTitle, final String methodName, final Map<String, Long> advancedValues)
			throws GeneralSecurityException, IOException, URISyntaxException, NeotysAPIException, ODataException {
		if (!proxyConfig.getDataExchangeAPIClient().isPresent()) {
			throw new RuntimeException("The DataExchangeAPIClient was not initialized properly.");
		}

		// set the data.
		final List<String> entryPath = createPath(currentURL, pageTitle);
		final EntryBuilder ebNormal = new EntryBuilder(entryPath, startTime);
		ebNormal.url(currentURL);
		ebNormal.status(Statuses.newStatus(methodName, exception));
		final double value = (double)System.currentTimeMillis() - startTime;
		ebNormal.value(value);
		ebNormal.unit(TimerBuilder.DEFAULT_UNIT);

		final List<Entry> entriesToSend = new ArrayList<>();
		entriesToSend.add(ebNormal.build());
		
		for (final Map.Entry<String, Long> mapEntry: advancedValues.entrySet()) {
		    final List<String> advancedPath = new ArrayList<>(entryPath);
		    
		    final String[] pathEntries = mapEntry.getKey().split("/");
		    advancedPath.addAll(Arrays.asList(pathEntries));

		    final EntryBuilder ebAdv = new EntryBuilder(advancedPath, startTime);
		    ebAdv.url(currentURL);
		    ebAdv.status(Statuses.newStatus(methodName, exception));
		    ebAdv.value(value);
	        ebAdv.unit(TimerBuilder.DEFAULT_UNIT);
	        ebAdv.value(Double.valueOf(mapEntry.getValue()));
		    
		    entriesToSend.add(ebAdv.build());
		}
		
        // send the data.
        final DataExchangeAPIClient dataExchangeAPIClient = proxyConfig.getDataExchangeAPIClient().get();
        
        for (final Entry entry: entriesToSend) {
            SeleniumProxyConfig.debugMessage("Sending entry. URL: " + currentURL + ", Title: " + pageTitle + ", Entry: "+ entry);
        }
        
        dataExchangeAPIClient.addEntries(entriesToSend);
	}

	List<String> createPath(final String currentURL, final String pageTitle) {
		final List<String> path = proxyConfig.newPath();

		final String customName = proxyConfig.getCustomName();
        if (customName != null) {
            final ArrayList<String> pathItems = Lists.newArrayList(Splitter.on("/").omitEmptyStrings().split(customName));
            path.addAll(pathItems);
			// make sure the custom name is only used once.
			proxyConfig.setCustomName(null);
		} else if (PathNamingPolicy.URL.equals(proxyConfig.getPathNamingPolicy())) {
			final String prettyURL = getPrettyURL(currentURL, proxyConfig.getRegexToCleanURLs());
            path.addAll(Lists.newArrayList(Splitter.on("/").omitEmptyStrings().split(prettyURL)));

		} else if (PathNamingPolicy.ACTION.equals(proxyConfig.getPathNamingPolicy())) {
			path.add(proxyConfig.getLastAction());

		} else if (PathNamingPolicy.TITLE.equals(proxyConfig.getPathNamingPolicy())) {
		    if (pageTitle == null) {
		        path.add(pageTitle);
		    } else {
		        path.addAll(Lists.newArrayList(Splitter.on("/").omitEmptyStrings().split(pageTitle)));
		    }

		} else {
			throw new RuntimeException("Unrecognized PathNamingPolicy");
		}
		
		return path;
	}

	/**
	 * @param urlStr
	 * @return
	 */
	private static String getPrettyURL(final String urlStr, final String regexURLCleaner) {
		String cleanedURL = urlStr;
		try {
			final URL url = new URL(StringUtils.trimToEmpty(urlStr));
			cleanedURL = urlStr.substring(urlStr.indexOf(url.getPath(), url.getHost().length() + "//".length()));
			cleanedURL = URLDecoder.decode(cleanedURL, "UTF-8");
		} catch (final Exception e) {
			// don't care.
		}

		final Pattern compile = Pattern.compile(regexURLCleaner);
		final Matcher matcher = compile.matcher(cleanedURL);
		if (matcher.matches()) {
			final StringBuilder newURL = new StringBuilder();
			for (int i = 1; i <= matcher.groupCount(); i++) {
				newURL.append(ObjectUtils.firstNonNull(matcher.group(i), ""));
			}
			cleanedURL = newURL.toString();
		}

		return cleanedURL;

	}

	/** Throw the stored exception if there is one. */
	void throwStoredException() {
		try {
			if (exception != null) {
				throw exception;
			}
		} finally {
			exception = null;
		}
	}
}
