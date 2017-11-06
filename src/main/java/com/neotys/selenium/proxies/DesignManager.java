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
import com.google.common.base.Preconditions;
import com.neotys.rest.design.client.DesignAPIClient;
import com.neotys.rest.design.model.ContainsUserPathParams.ContainsUserPathParamsBuilder;
import com.neotys.rest.design.model.IsProjectOpenParams.IsProjectOpenParamsBuilder;
import com.neotys.rest.design.model.OpenProjectParams.OpenProjectParamsBuilder;
import com.neotys.rest.design.model.StartRecordingParams.StartRecordingBuilder;
import com.neotys.rest.design.model.StopRecordingParams.StopRecordingBuilder;
import com.neotys.rest.error.NeotysAPIException;
import com.neotys.selenium.proxies.helpers.SeleniumProxyConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;

/**
 * Handle start operations (close previous project, open current project and start recording)
 * and stop operations (stop recording and close current project).
 * Created by anouvel on 08/11/2016.
 */
public class DesignManager {

	private static final int SLEEP_TIME = 2000;

	public static DesignAPIClient getDesignApiClient() {
		Preconditions.checkState(SeleniumProxyConfig.getDesignAPIClient().isPresent(), "Design API client is not initialized.");
		return SeleniumProxyConfig.getDesignAPIClient().get();
	}

	private final String userPathName;
	private final Optional<String> projectPath;
	private final ParamBuilderProvider paramBuilderProvider;
	private boolean userPathExist;

	public DesignManager(final String userPathName, final Optional<String> projectPath, final ParamBuilderProvider paramBuilderProvider) {
		this.userPathName = userPathName;
		this.projectPath = projectPath;
		this.paramBuilderProvider = paramBuilderProvider;
	}

	/**
	 * If a projectPath is provided then if necessary previous project is closed and project denoted by projectPath is opened.
	 * After that, the recording is started.
	 */
	public void start() {
		final DesignAPIClient designAPIClient = getDesignApiClient();

		if (projectPath.isPresent()) {
			openProject(designAPIClient);
		}

		this.userPathExist = containsUserPath(designAPIClient);

		final StartRecordingBuilder startRecordingBuilder = paramBuilderProvider.newStartRecordingBuilder();
		if (!userPathExist) {
			startRecordingBuilder.virtualUser(userPathName);
		} else {
			startRecordingBuilder.virtualUser(userPathName + "_recording");
		}

		try {
			designAPIClient.startRecording(startRecordingBuilder.build());
		} catch (IOException | GeneralSecurityException | URISyntaxException | NeotysAPIException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private boolean containsUserPath(final DesignAPIClient designAPIClient) {
		try {
			return designAPIClient.containsUserPath(new ContainsUserPathParamsBuilder().name(userPathName).build());
		} catch (final IOException | GeneralSecurityException | URISyntaxException | NeotysAPIException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void openProject(final DesignAPIClient designAPIClient) {
		try {
			final boolean isOpen = designAPIClient.isProjectOpen(new IsProjectOpenParamsBuilder().filePath(projectPath.get()).build());
			if (!isOpen) {
				designAPIClient.closeProject(paramBuilderProvider.newCloseProjectParamsBuilder().build());
				designAPIClient.openProject(new OpenProjectParamsBuilder().filePath(projectPath.get()).build());
			}
		} catch (final IOException | GeneralSecurityException | URISyntaxException | NeotysAPIException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stop the recording and save the project.
	 */
	public void stop() {
		final StopRecordingBuilder stopRecordParams = paramBuilderProvider.newStopRecordingBuilder();
		if (userPathExist) {
			stopRecordParams.updateParams(paramBuilderProvider.newUpdateUserPathParamsBuilder().name(userPathName).build());
		}
		try {
			getDesignApiClient().stopRecording(stopRecordParams.build());
		} catch (final IOException | GeneralSecurityException | URISyntaxException | NeotysAPIException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		try {
			getDesignApiClient().saveProject();
		} catch (final IOException | GeneralSecurityException | URISyntaxException | NeotysAPIException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
}
