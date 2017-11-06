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

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by anouvel on 08/11/2016.
 */
public class ModeHelper {

	/**
	 * All variables begin with this prefix.
	 */
	static final String OPT_PREFIX = "nl.";

	public static final String OPT_SELENIUM_WRAPPER_MODE = OPT_PREFIX + "selenium.proxy.mode";

	/**
	 * An environment variable.
	 */
	public static final String OPT_SELENIUM_WRAPPER_ENABLED = OPT_PREFIX + "selenium.proxy.enabled";

	public static final String MODE_NO_API = "NoApi";
	public static final String MODE_DESIGN = "Design";
	public static final String MODE_END_USER_EXPERIENCE = "EndUserExperience";

	public enum Mode {
		DESIGN, END_USER_EXPERIENCE, NO_API
	}

	public static Mode getMode() {
		final String modeString = ModeHelper.getSetting(OPT_SELENIUM_WRAPPER_MODE, MODE_NO_API);
		if (MODE_DESIGN.equalsIgnoreCase(modeString)) {
			return Mode.DESIGN;
		} else if (MODE_END_USER_EXPERIENCE.equalsIgnoreCase(modeString)) {
			return Mode.END_USER_EXPERIENCE;
		} else if (MODE_NO_API.equalsIgnoreCase(modeString)) {
			System.setProperty(OPT_SELENIUM_WRAPPER_ENABLED, "false");
			return Mode.NO_API;
		} else {
			throw new IllegalArgumentException("Unknown mode: " + modeString);
		}
	}

	/**
	 * Used for reading command line arguments when the user passed arguments instead of environment variables for settings.
	 */
	static final String COMMAND_LINE_ARGS[] = StringUtils.trimToEmpty(System.getProperty("sun.java.command")).split("\\s");

	public static String getSetting(final String key, final String defaultValue) {
		// look for the environment variable
		if (System.getProperty(key) != null) {
			return System.getProperty(key);
		}

		// look for a program argument
		final Pattern pattern = Pattern.compile("-D" + Pattern.quote(key) + "=" + "(.+)");
		for (final String arg : COMMAND_LINE_ARGS) {
			final Matcher matcher = pattern.matcher(arg);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}

		return defaultValue;
	}
}
