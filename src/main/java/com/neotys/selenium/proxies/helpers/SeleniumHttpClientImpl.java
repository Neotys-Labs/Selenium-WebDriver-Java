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

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.jayway.jsonpath.JsonPath;
import com.neotys.extensions.action.perfecto.NLLogger;
import com.neotys.extensions.action.perfecto.api.PerfectoConnectionParameters;
import com.neotys.extensions.action.perfecto.api.PerfectoHttpClientImpl;
import com.neotys.extensions.action.perfecto.data.PerfectoScriptResults;
import com.neotys.extensions.action.perfecto.data.PerfectoScriptResultsContext;
import com.neotys.selenium.proxies.helpers.perfecto.PerfectoLabUtils;

public class SeleniumHttpClientImpl extends PerfectoHttpClientImpl {

	/** Download reports etc. */
	private final RemoteWebDriver webDriver;
	
	private final NLLogger logger;
	
	/** Log various messages. */
	public SeleniumHttpClientImpl(final NLLogger logger, final PerfectoConnectionParameters perfectoConnectionParameters, 
			final PerfectoScriptResultsContext perfectoScriptResultsContext, final RemoteWebDriver webDriver) {
		super(logger, perfectoConnectionParameters);
		
		this.perfectoScriptResultsContext = perfectoScriptResultsContext;
		this.webDriver = webDriver;
		this.logger = logger;
	}

	@Override
	public PerfectoScriptResults downloadReports() throws IOException {
		checkNotNull(perfectoScriptResultsContext, ERROR_MSG_NO_SCRIPT_EXECUTED);
		
		final String reportContentTempFilenameXML = perfectoScriptResultsContext.getReportKey().replaceAll("[^a-zA-Z0-9.-]", "_");
		final File perfectoScriptResultsFileXML = File.createTempFile(reportContentTempFilenameXML + "_", ".xml");
		createdTempFiles.add(perfectoScriptResultsFileXML.getAbsolutePath());
		
		final String reportContentTempFilenameHTML = perfectoScriptResultsContext.getReportKey().replaceAll("[^a-zA-Z0-9.-]", "_");
		final File perfectoScriptResultsFileHTML =
				File.createTempFile(FilenameUtils.getBaseName(reportContentTempFilenameHTML) + "_", ".html");
		createdTempFiles.add(perfectoScriptResultsFileHTML.getAbsolutePath());

		String responseContentHTML = "";
		String responseContentXML = "";
		
		boolean downloadSuccessful = false;
		Files.delete(perfectoScriptResultsFileHTML.toPath());
		logger.debug("Downloading report to " + perfectoScriptResultsFileHTML.toPath());
		responseContentHTML = PerfectoLabUtils.downloadReport(webDriver, "html", perfectoScriptResultsFileHTML.getAbsolutePath());
		FileUtils.write(new File(perfectoScriptResultsFileHTML.getAbsolutePath()), responseContentHTML);

		Files.delete(perfectoScriptResultsFileXML.toPath());
		logger.debug("Downloading report to " + perfectoScriptResultsFileXML.toPath());
		responseContentXML = PerfectoLabUtils.downloadReport(webDriver, "xml", perfectoScriptResultsFileXML.getAbsolutePath());
		FileUtils.write(new File(perfectoScriptResultsFileXML.getAbsolutePath()), responseContentXML);
		
		try {
			// if this succeeds then we received json content with an error message.
			JsonPath.read(responseContentXML, "$.errorMessage");
			JsonPath.read(responseContentHTML, "$.errorMessage");
		} catch (final Exception e) {
			// there must be no error message and the content must be non-empty. otherwise it's not a report file.
			if (responseContentXML.length() > 0 && responseContentHTML.length() > 0) {
				downloadSuccessful = true;
			}
		}

		if (!downloadSuccessful) {
			throw new IOException("Issue downloading report file for script \"" + perfectoScriptResultsContext.getRemoteScriptKey() +
					"\": " + responseContentXML);
		}
		
		final PerfectoScriptResults perfectoScriptResults = new PerfectoScriptResults();
		perfectoScriptResults.setReportDataXMLPath(perfectoScriptResultsFileXML.getAbsolutePath());
		perfectoScriptResults.setReportDataHTMLPath(perfectoScriptResultsFileHTML.getAbsolutePath());
		
		return perfectoScriptResults;
	}
}
