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

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.neotys.extensions.action.perfecto.NLLogger;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClient;
import com.neotys.rest.dataexchange.client.DataExchangeAPIClientFactory;
import com.neotys.rest.dataexchange.model.Context;
import com.neotys.rest.dataexchange.model.ContextBuilder;
import com.neotys.rest.dataexchange.model.TimerBuilder;
import com.neotys.rest.design.client.DesignAPIClient;
import com.neotys.rest.design.client.DesignAPIClientFactory;
import com.neotys.rest.design.model.SetContainerParams;
import com.neotys.rest.error.NeotysAPIException;
import com.neotys.selenium.proxies.CustomProxyConfig;
import com.neotys.selenium.proxies.TransactionModifier;
import org.apache.commons.lang3.StringUtils;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import static com.neotys.selenium.proxies.helpers.ModeHelper.Mode.DESIGN;
import static com.neotys.selenium.proxies.helpers.ModeHelper.Mode.END_USER_EXPERIENCE;
import static com.neotys.selenium.proxies.helpers.ModeHelper.*;


public class SeleniumProxyConfig implements CustomProxyConfig, NLLogger, TransactionModifier {

	private static final String TRANSACTION_TIMER_NAME = "Timer";

	/** Absent means we haven't checked yet. If true then we get the name of the JUnit method to use in the path part of the Entry. */
    private static Optional<Boolean> hasJunit = Optional.absent();

    /** An environment variable. */
    public static final String OPT_DATA_EXCHANGE_URL = OPT_PREFIX + "data.exchange.url";

	/** An environment variable. */
	public static final String OPT_DESIGN_API_URL = OPT_PREFIX + "design.api.url";

    /** An environment variable. */
    public static final String OPT_DEBUG = OPT_PREFIX + "debug";

    /** An environment variable. */
    public static final String OPT_API_KEY = OPT_PREFIX + "api.key";

    /** An environment variable. */
    public static final String OPT_LOCATION = OPT_PREFIX + "location";

    /** An environment variable. */
    public static final String OPT_SOFTWARE = OPT_PREFIX + "software";

    /** An environment variable. */
    public static final String OPT_OS = OPT_PREFIX + "os";

    /** An environment variable. */
    public static final String OPT_HARDWARE = OPT_PREFIX + "hardware";

    /** An environment variable. */
    public static final String OPT_INSTANCE_ID = OPT_PREFIX + "instance.id";

    /** An environment variable. */
    public static final String OPT_SCRIPT_NAME = OPT_PREFIX + "script.name";

    /** An environment variable. */
    public static final String OPT_PATH_NAMING_POLICY = OPT_PREFIX + "path.naming.policy";

    /** An environment variable. */
    public static final String OPT_REGEX_TO_CLEAN_URLS = OPT_PREFIX + "regex.to.clean.urls";

    /** An environment variable. */
    public static final String OPT_NAVIGATION_TIMING = OPT_PREFIX + "navigation.timing.enabled";

    /** When no script name is provided then this is used. */
    private static final String DEFAULT_SCRIPT_NAME_PREFIX = "SeleniumDelegate";

    /** Used for the context. */
    private static final String defaultScriptName = DEFAULT_SCRIPT_NAME_PREFIX + getJUnitTestName();

	private static final String OPT_CAPABILITIES_PREFIX = OPT_PREFIX + "capabilities.";

	/** Environment variables used to override the key used to find device information in capabilities. */
	public static final String OPT_CAPABILITIES_PLATFORM_NAME = OPT_CAPABILITIES_PREFIX + "platform.name";
	public static final String OPT_CAPABILITIES_PLATFORM_VERSION = OPT_CAPABILITIES_PREFIX + "platform.version";
	public static final String OPT_CAPABILITIES_DEVICE_NAME = OPT_CAPABILITIES_PREFIX + "device.name";
	public static final String OPT_CAPABILITIES_BROWSER_NAME = OPT_CAPABILITIES_PREFIX + "browser.name";
	public static final String OPT_CAPABILITIES_BROWSER_VERSION = OPT_CAPABILITIES_PREFIX + "browser.version";
	public static final String OPT_CAPABILITIES_LOCATION = OPT_CAPABILITIES_PREFIX + "location";

	public enum PathNamingPolicy {
		/** Parse the URL to create a path. */
		URL,

		/** Use the title. */
		TITLE,

		/** Use a string representing the most recent action that was done. */
		ACTION
	}

	/** A string identifying the most recent action that was done. For example click() or select(). */
	private String lastAction = "(none)";

	/** A one-time-use name to use when creating a path. */
	private String customName;

	private Optional<String> userPathName = Optional.absent();
	private String transactionName;

	private TimerBuilder timerBuilder = null;

	/** Which naming policy to use when creating the path of an entry. */
	private PathNamingPolicy pathNamingPolicy = SeleniumProxyConfig.PathNamingPolicy.URL;

	/** The regular expression used to clean a URL when building a path. */
	private static final String DEFAULT_REGEX_TO_CLEAN_URLS = "(.*?)[#?;%].*";

	/** The regular expression used to clean a URL when building a path. */
	private String regexToCleanURLs = DEFAULT_REGEX_TO_CLEAN_URLS;

	/** Sends data to the NeoLoad controller. */
	private static Optional<DataExchangeAPIClient> dataExchangeAPIClient;

	/** Sends order to the NeoLoad controller. */
	private static Optional<DesignAPIClient> designAPIClient;

	/** Used for the default software type. FirefoxDriver, IE, Chrome, etc. */
	private final String driverType;

	/** How to access NeoLoad. */
	private static String designAPIURL = "http://localhost:7400/Design/v1/Service.svc/";

	/** How to access NeoLoad. */
	private String dataExchangeAPIURL = "http://localhost:7400/DataExchange/v1/Service.svc/";
	/** How to access NeoLoad. */
	private String dataExchangeAPIKey = "";
	/** For sending to NeoLoad. */
	private String instanceID = "";

	/** Used to download Perfecto reports and files. */
	private String perfectoUser;
	/** Used to download Perfecto reports and files. */
	private String perfectoPassword;

	/** Used to populate the context of the Data Exchange API client. */
	private Optional<Capabilities> capabilities = Optional.absent();

	static{
		designAPIClient = initializeDesignAPIClient();
	}

    /** Constructor.
     * @param driverType FirefoxDriver, IE, Chrome, etc. Used for the default software type.
     */
    public SeleniumProxyConfig(final String driverType) {
        debugMessage("---------- Passed in argument information. BEGIN: ");
        for (final Entry<Object, Object> entry: System.getProperties().entrySet()) {
            if (entry.getKey().toString().startsWith(OPT_PREFIX)) {
                debugMessage("Environment variable: Key: " + entry.getKey() + ", value: " + entry.getValue());
            }
        }
        for (final String arg: ModeHelper.COMMAND_LINE_ARGS) {
            if (arg.matches("-D.+=.+")) {
                debugMessage("Command line argument: " + arg);
            }
        }
        debugMessage("---------- Passed in argument information. END");

        // read the passed in values if there are any.
        final String value = getSettingNoDefault(OPT_PATH_NAMING_POLICY);
        if (value != null) {
            pathNamingPolicy = PathNamingPolicy.valueOf(value.toUpperCase().trim());
        }

        final String regex = getSettingNoDefault(OPT_REGEX_TO_CLEAN_URLS);
        if (regex != null) {
            regexToCleanURLs = regex;
        }

        this.driverType = driverType;
    }

    public String getDataExchangeAPIURL() {
		return dataExchangeAPIURL;
	}
    public String getDataExchangeAPIKey() {
		return dataExchangeAPIKey;
	}
    public String getInstanceID() {
		return instanceID;
	}

    /** @return true if the debug option was passed in. */
    private static boolean isDebug() {
        return ("true".equalsIgnoreCase(getSettingNoDefault(OPT_DEBUG)));
    }

    /** @param msg */
    static void debugMessage(final String msg) {
        if (isDebug()) {
            System.out.println(SeleniumProxyConfig.class.getSimpleName() + " DEBUG: " + msg);
        }
    }

	@Override
	public void info(String msg) {
        System.out.println(SeleniumProxyConfig.class.getSimpleName() + " INFO: " + msg);
	}

    static void errorMessage(final String msg) {
        System.err.println(SeleniumProxyConfig.class.getSimpleName() + " ERROR: " + msg);
    }

	public static String getDesignAPIURL() {
		return designAPIURL;
	}

	public static Optional<DesignAPIClient> getDesignAPIClient() {
		return designAPIClient;
	}

	private static Optional<DesignAPIClient> initializeDesignAPIClient() {
		designAPIURL = getSetting(OPT_DESIGN_API_URL, designAPIURL);
		/* How to access NeoLoad. */
		final String designAPIKey = getSetting(OPT_API_KEY, "");

		DesignAPIClient designAPIClient = null;
		try {
			debugMessage("Connecting to design API server. URL: " + designAPIURL + ", API key: " + designAPIKey);
			designAPIClient = DesignAPIClientFactory.newClient(designAPIURL, designAPIKey);
		} catch (final GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {
			if (!isEnabled() || !DESIGN.equals(ModeHelper.getMode())) {
				return Optional.absent();
			}
			// throw exception only when design API client is required.
			throw new RuntimeException("Issue contacting DesignAPI server.", e);
		}
		return Optional.of(designAPIClient);
	}

	public synchronized Optional<DataExchangeAPIClient> getDataExchangeAPIClient() {
	    if (dataExchangeAPIClient == null) {
	        dataExchangeAPIClient = initializeDataExchangeAPIClient();
	    }

		return dataExchangeAPIClient;
	}

    private Optional<DataExchangeAPIClient> initializeDataExchangeAPIClient() {
        if (!isEnabled()) {
            return Optional.absent();
        }

        dataExchangeAPIURL = getSetting(OPT_DATA_EXCHANGE_URL, dataExchangeAPIURL);
        dataExchangeAPIKey = getSetting(OPT_API_KEY, "");

        // create a new context and prefer the user's custom settings over the defaults.
        final ContextBuilder cb = new ContextBuilder();
        cb.software(getSetting(OPT_SOFTWARE, getSoftware()));
        cb.os(getSetting(OPT_OS, getOS()));
        cb.hardware(getSetting(OPT_HARDWARE, getHardware()));

        instanceID = getSetting(OPT_INSTANCE_ID, getDefaultInstanceID());
        cb.instanceId(instanceID);

        cb.location(getSetting(OPT_LOCATION, getLocation()));
        cb.script(getScriptName());

        DataExchangeAPIClient dataExchangeAPIClient = null;
        try {
            final Context context = cb.build();
            debugMessage("Connecting to data exchange API server. URL: " + dataExchangeAPIURL +
                    ", API key: " + dataExchangeAPIKey + ", Context: " + context);
            dataExchangeAPIClient = DataExchangeAPIClientFactory.newClient(dataExchangeAPIURL, context, dataExchangeAPIKey);
        } catch (GeneralSecurityException | IOException | ODataException | URISyntaxException | NeotysAPIException e) {

            // give a more specific error message for a common configuration issue.
            if (StringUtils.trimToEmpty(dataExchangeAPIURL).toLowerCase().contains("localhost") &&
                e instanceof NeotysAPIException &&
                ((NeotysAPIException)e).getErrorType() == NeotysAPIException.ErrorType.NL_DATAEXCHANGE_NO_TEST_RUNNING) {

                throw new RuntimeException("Disable the proxy or set the server URL. "
                        + "If using a JAR file, settings must be specified before the JAR file. See documentation for details.", e);
            }

            throw new RuntimeException("Issue contacting DataExchangeAPI server. See documentation to disable the proxy.", e);
        }

        return Optional.of(dataExchangeAPIClient);
    }

	private String getOS() {
    	if(capabilities.isPresent()){
			final Object platformName = capabilities.get().getCapability(getSetting(OPT_CAPABILITIES_PLATFORM_NAME,"platformName"));
			if(platformName != null){
				final StringBuilder osBuilder = new StringBuilder();
				osBuilder.append(platformName.toString());
				final Object platformVersion = capabilities.get().getCapability(getSetting(OPT_CAPABILITIES_PLATFORM_VERSION,"platformVersion"));
				if(platformVersion != null){
					osBuilder.append(" ").append(platformName.toString());
				}
				return osBuilder.toString();
			}
		}
		return System.getProperty("os.name");
	}

	private String getSoftware() {
		if(capabilities.isPresent()){
			final Object browserName = capabilities.get().getCapability(getSetting(OPT_CAPABILITIES_BROWSER_NAME, "browserName"));
			if(browserName != null){
				final StringBuilder osBuilder = new StringBuilder();
				osBuilder.append(browserName.toString());
				final Object browserVersion = capabilities.get().getCapability(getSetting(OPT_CAPABILITIES_BROWSER_VERSION,"browserVersion"));
				if(browserVersion != null){
					osBuilder.append(" ").append(browserVersion.toString());
				}
				return osBuilder.toString();
			}
		}
		return driverType;
	}

	private String getHardware() {
		if(capabilities.isPresent()){
			final Object deviceName = capabilities.get().getCapability(getSetting(OPT_CAPABILITIES_DEVICE_NAME,"deviceName"));
			if(deviceName != null){
				return deviceName.toString();
			}
		}
		return "";
	}

	private String getLocation() {
		if(capabilities.isPresent()){
			final Object location = capabilities.get().getCapability(getSetting(OPT_CAPABILITIES_LOCATION,"location"));
			if(location != null){
				return location.toString();
			}
		}
		return "";
	}

	/** @return the lastAction */
	public String getLastAction() {
		return lastAction;
	}

	/** @param lastAction the lastAction to set */
	public SeleniumProxyConfig setLastAction(final String lastAction) {
		this.lastAction = lastAction;
		return this;
	}

	/** @return the customName */
	public String getCustomName() {
		return customName;
	}

	public String getTransactionName() {
		return transactionName;
	}

	/** Set a one-time-use custom name that is used for creating the Path of the next NeoLoad Entry that is created.
	 * @param customName the customName to set
	 * @return
	 */
	@Override
    public void setCustomName(final String customName) {
		this.customName = customName;
	}

	/** @return the pathNamingPolicy */
	public PathNamingPolicy getPathNamingPolicy() {
		return pathNamingPolicy;
	}

	/** @return the enabled */
	public static boolean isEnabled() {
	    // default to enabled
        final String actualValue = getSettingNoDefault(OPT_SELENIUM_WRAPPER_ENABLED);
        if (actualValue == null) {
            return true;
        }

        return Boolean.valueOf("" + actualValue);
	}

    /** @return the enabled */
    public static boolean isNavigationTimingEnabled() {
        // default to enabled
        final String actualValue = getSettingNoDefault(OPT_NAVIGATION_TIMING);
        if (actualValue == null) {
            return true;
        }

        return Boolean.valueOf("" + actualValue);
    }

    /**
     * @param key
     * @return
     */
    private static String getSettingNoDefault(final String key) {
        return getSetting(key, null);
    }

	public String getScriptName() {
		return getSetting(OPT_SCRIPT_NAME, defaultScriptName);
	}

	/** @return the regexToCleanURLs */
	@Override
    public String getRegexToCleanURLs() {
		return regexToCleanURLs;
	}

    /**
     * @return
     */
    private static String getDefaultInstanceID() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss");
		return sdf.format(new Date());
    }

    /** Returns the name of the junit test or an empty string if one is not found.
     * @return the most recent method on the stack annotated as a test.
     */
    private static synchronized <T> String getJUnitTestName() {
        // check if we have junit or not.
        if (!hasJunit.isPresent()) {
            try {
                Class.forName("org.junit.Test");
                hasJunit = Optional.of(true);

            } catch (final Throwable t) {
                hasJunit = Optional.of(false);
            }
        }

        // if we don't have junit then return.
        if (!hasJunit.get()) {
            return "";
        }

        // default to an empty string
        String testName = "";
        final StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        // look at all elements in reverse order until we find one annotated as a junit test.
        for (int i = trace.length - 1; i >= 0; i--) {
            try {
                final StackTraceElement stackTraceElement = trace[i];
                final Class<?> clazz = Class.forName(stackTraceElement.getClassName());
                final String methodName = stackTraceElement.getMethodName();
                final Method declaredMethod = clazz.getDeclaredMethod(methodName);
                @SuppressWarnings("unchecked")
                final Class<Annotation> annotationClass = (Class<Annotation>) Class.forName("org.junit.Test");
                final Annotation annotation = declaredMethod.getAnnotation(annotationClass);
                if (annotation != null) {
                    // only use the first one we find.
                    testName = methodName;
                    break;
                }
            } catch (final Throwable e) {
                // ignored
            }
        }

        if (testName.length() > 0) {
            return "-" + testName;
        }

        return testName;
    }

	@Override
	public void debug(final String msg) {
		debugMessage(msg);
	}

	@Override
	public void error(String msg) {
		errorMessage(msg);
	}

	public boolean isDebugEnabled() {
		return isDebug();
	}

	@Override
	public void error(String msg, Throwable exception) {
		error(msg);
		exception.printStackTrace();
	}

	/**
	 * @return the perfectoUser
	 */
	public String getPerfectoUser() {
		return perfectoUser;
	}

	/**
	 * @param perfectoUser the perfectoUser to set
	 */
	public void setPerfectoUser(String perfectoUser) {
		this.perfectoUser = perfectoUser;
	}

	/**
	 * @return the perfectoPassword
	 */
	public String getPerfectoPassword() {
		return perfectoPassword;
	}

	/**
	 * @param perfectoPassword the perfectoPassword to set
	 */
	public void setPerfectoPassword(String perfectoPassword) {
		this.perfectoPassword = perfectoPassword;
	}

	/**
	 * @param webDriver
	 * @return true if we're using perfecto
	 */
	public boolean isUsingPerfecto(final WebDriver webDriver) {
		if (!(webDriver instanceof RemoteWebDriver)) {
			return false;
		}

		final String perfectoSearchString = "" + ((RemoteWebDriver) webDriver).getCapabilities().getCapability("host") +
				((RemoteWebDriver) webDriver).getCapabilities().getCapability("singleTestReportUrl") +
				((RemoteWebDriver) webDriver).getCapabilities().getCapability("windTunnelReportUrl");
		if (!perfectoSearchString.toLowerCase().contains("perfecto")) {
			return false;
		}

		return true;
	}

	public void setUserPathName(final Optional<String> userPathName){
		this.userPathName = userPathName;
	}

	public Optional<String> getUserPathName() {
		return userPathName;
	}

	public void setCapabilities(final Optional<Capabilities> capabilities) {
		this.capabilities = capabilities;
	}

	@Override
	public void startTransaction(final String name) {
		if(DESIGN.equals(ModeHelper.getMode()) && getDesignAPIClient().isPresent()){
			try {
				getDesignAPIClient().get().setContainer(new SetContainerParams(name));
			} catch (final GeneralSecurityException | IOException | NeotysAPIException | URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}else if (END_USER_EXPERIENCE.equals(ModeHelper.getMode())) {
			transactionName = name;
			handleTimer();
			final List<String> timerPath = newPath();
			timerPath.add(TRANSACTION_TIMER_NAME);
			timerBuilder = TimerBuilder.start(timerPath);
		}
	}

	public List<String> newPath() {
		final List<String> path = new ArrayList<>(5);

		path.add(getScriptName());
		path.add(TimerBuilder.TIMERS_NAME);
		if(getUserPathName().isPresent()){
			path.add(getUserPathName().get());
		}
		if(!Strings.isNullOrEmpty(transactionName)) {
			path.add(transactionName);
		}

		return path;
	}

	private void handleTimer(){
		final TimerBuilder current = timerBuilder;
		if(current != null && getDataExchangeAPIClient().isPresent()) {
			try {
				getDataExchangeAPIClient().get().addEntry(current.stop());
			} catch (GeneralSecurityException | IOException | URISyntaxException | NeotysAPIException e) {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void stopTransaction() {
		if(END_USER_EXPERIENCE.equals(ModeHelper.getMode())) {
			handleTimer();
			transactionName = null;
			timerBuilder = null;
		}
	}
}
