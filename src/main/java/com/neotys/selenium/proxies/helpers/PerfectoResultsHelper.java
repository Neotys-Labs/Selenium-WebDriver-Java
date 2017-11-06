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
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.olingo.odata2.api.exception.ODataException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.xml.sax.SAXException;

import com.neotys.extensions.action.ActionParameter;
import com.neotys.extensions.action.perfecto.NLLogger;
import com.neotys.extensions.action.perfecto.PerfectoMethodExecutor;
import com.neotys.extensions.action.perfecto.api.PerfectoConnectionParameters;
import com.neotys.extensions.action.perfecto.api.PerfectoHttpClient;
import com.neotys.extensions.action.perfecto.data.PerfectoCommandLineArguments;
import com.neotys.extensions.action.perfecto.data.PerfectoScriptResults;
import com.neotys.extensions.action.perfecto.data.PerfectoScriptResultsContext;
import com.neotys.extensions.action.perfecto.entry.APIDataEntryAll;
import com.neotys.extensions.action.perfecto.utils.PerfectoResultsParser;
import com.neotys.extensions.action.perfecto.utils.PerfectoResultsParserImpl;
import com.neotys.rest.error.NeotysAPIException;

public class PerfectoResultsHelper {
	
	private final SeleniumProxyConfig proxyConfig;
	
	private final RemoteWebDriver webDriver;
	
	/** When the script started. */
	private final long actualScriptStartTime;
	
	/** When true then we don't resend the data. */
	private boolean alreadySent = false;
	
	private final NLLogger logger;

	public PerfectoResultsHelper(final ProxySendHelper proxySendHelper, @Nonnull final RemoteWebDriver webDriver,
			final SeleniumProxyConfig proxyConfig, final long actualScriptStartTime) {
		this.webDriver = webDriver;
		this.proxyConfig = proxyConfig;
		this.actualScriptStartTime = actualScriptStartTime;
		this.logger = proxyConfig;
	}

	public void sendResults() {
		if (alreadySent) {
			return;
		}
		// report key is PRIVATE:RemoteWebDriver/RemoteWebDriver_16-04-15_13_02_08_10203.xml
		final String reportKey = "" + webDriver.getCapabilities().getCapability("reportKey");
		String executionID = reportKey;
		String scriptName = reportKey;
		try {
			// PRIVATE:RemoteWebDriver/RemoteWebDriver_16-04-15_13_02_08_10203.xml  ->  RemoteWebDriver 
			scriptName = reportKey.replaceAll(".*/(.+?)_(\\d{2}-\\d{2}-\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d+)[.].*", "$1");
			// PRIVATE:RemoteWebDriver/RemoteWebDriver_16-04-15_13_02_08_10203.xml  ->  16-04-15_13_02_08_10203 
			executionID = reportKey.replaceAll(".*/(.+?)_(\\d{2}-\\d{2}-\\d{2}_\\d{2}_\\d{2}_\\d{2}_\\d+)[.].*", "$2");

		} catch (final Exception e) {
			// oh well. don't care.
		}
		
		// create some necessary variables.
		final PerfectoCommandLineArguments perfectoCommandLineArguments = createPerfectoCommandLineArguments(executionID, scriptName);

		// create some necessary variables.
		final PerfectoConnectionParameters perfectoConnectionParameters = new PerfectoConnectionParameters(
				perfectoCommandLineArguments.getParsedArgs(), perfectoCommandLineArguments);
		final String responseContent = "{\"" + 
				PerfectoScriptResultsContext.JSON_FIELD_EXECUTION_ID + "\": \"" + 
				webDriver.getCapabilities().getCapability("executionId") + 
				"\", \"" + PerfectoScriptResultsContext.JSON_FIELD_REPORT_KEY + "\": \"" + reportKey + "\" }";
		final PerfectoScriptResultsContext perfectoScriptResultsContext = 
				new PerfectoScriptResultsContext(scriptName, responseContent, actualScriptStartTime);
		
		// parse the results
		logger.debug("Parsing Perfecto results.");
		final List<APIDataEntryAll> apiDataEntryAllList = new ArrayList<>();
		final PerfectoMethodExecutor perfectoMethodExecutor = new PerfectoMethodExecutor(proxyConfig);
		apiDataEntryAllList.addAll(parsePerfectoResults(perfectoConnectionParameters, perfectoScriptResultsContext,
				apiDataEntryAllList, perfectoMethodExecutor));

		// send the data to the openAPI.
		try {
			logger.debug("Sending Perfecto results to Data Exchange API.");
			perfectoMethodExecutor.sendResults(proxyConfig.getDataExchangeAPIURL(),
					proxyConfig.getDataExchangeAPIKey(),
					apiDataEntryAllList, perfectoScriptResultsContext.getExecutionId());
		
			alreadySent = true;
		} catch (final GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			proxyConfig.error("Error sending parsed Perfecto results to the Data Exchange API.", e);
		}
	}

	/**
	 * @param perfectoConnectionParameters
	 * @param perfectoScriptResultsContext
	 * @param apiDataEntryAllList
	 * @param perfectoMethodExecutor
	 * @return
	 */
	private List<APIDataEntryAll> parsePerfectoResults(final PerfectoConnectionParameters perfectoConnectionParameters,
			final PerfectoScriptResultsContext perfectoScriptResultsContext, List<APIDataEntryAll> apiDataEntryAllList,
			final PerfectoMethodExecutor perfectoMethodExecutor) {
		
		final PerfectoResultsParser perfectoResultsParser = new PerfectoResultsParserImpl(proxyConfig);
		final PerfectoHttpClient perfectoHttpClient = 
				new SeleniumHttpClientImpl(proxyConfig, perfectoConnectionParameters, perfectoScriptResultsContext,
						webDriver);
		try {
			perfectoMethodExecutor.setPerfectoHttpClient(perfectoHttpClient);
			final PerfectoScriptResults perfectoScriptResults = perfectoMethodExecutor.extractResults(perfectoResultsParser);
			apiDataEntryAllList = perfectoMethodExecutor.scriptResultsToAPIData(perfectoScriptResultsContext.getRemoteScriptKey(),
					perfectoConnectionParameters.getInstanceID(),
			perfectoScriptResultsContext, perfectoScriptResults, perfectoResultsParser);
			
			// this deletes the temporary files.
			perfectoHttpClient.cleanup(false);
			
		} catch (final XPathExpressionException | IOException | ParserConfigurationException | SAXException e) {
			proxyConfig.error("Error parsing Perfecto results.", e);
			perfectoHttpClient.cleanup(true);
		}
		return apiDataEntryAllList;
	}

	/**
	 * @param executionID
	 * @param scriptName
	 * @return
	 */
	private PerfectoCommandLineArguments createPerfectoCommandLineArguments(final String executionID,
			final String scriptName) {
		final List<ActionParameter> parameters = new ArrayList<>();
		URI uri = null;
		try {
			uri = new URI("" + webDriver.getCapabilities().getCapability("windTunnelReportUrl"));
			
		} catch (final URISyntaxException e1) {
			// https://demo.perfectomobile.com/services/
			try {
				uri = new URI("https://" + webDriver.getCapabilities().getCapability("host") + "/services/");
			} catch (final URISyntaxException e) {
				proxyConfig.error("Issue figuring out Perfecto API URL.", e);
				return null;
			}
		}

		// e.g. https://demo.perfectomobile.com/services/
		final String perfectoAPIURL = uri.getScheme() + "://" + uri.getHost() + "/services/";
		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.PerfectoAPIURL.getName(), 
				perfectoAPIURL, PerfectoCommandLineArguments.PerfectoOptions.PerfectoAPIURL.getType()));

		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.Username.getName(), 
				"PerfectoUser()", PerfectoCommandLineArguments.PerfectoOptions.Username.getType()));

		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.Password.getName(), 
				"PerfectoPassword()", PerfectoCommandLineArguments.PerfectoOptions.Password.getType()));

		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.Script.getName(), 
				scriptName, PerfectoCommandLineArguments.PerfectoOptions.Script.getType()));

		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.DataExchangeAPIURL.getName(), 
				proxyConfig.getDataExchangeAPIURL(), PerfectoCommandLineArguments.PerfectoOptions.DataExchangeAPIURL.getType()));

		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.InstanceID.getName(), 
				executionID, PerfectoCommandLineArguments.PerfectoOptions.InstanceID.getType()));

		parameters.add(new ActionParameter(PerfectoCommandLineArguments.PerfectoOptions.DataExchangeAPIKey.getName(), 
				proxyConfig.getDataExchangeAPIKey(), PerfectoCommandLineArguments.PerfectoOptions.DataExchangeAPIKey.getType()));

		final PerfectoCommandLineArguments perfectoCommandLineArguments = new PerfectoCommandLineArguments(proxyConfig, parameters);
		return perfectoCommandLineArguments;
	}

}
