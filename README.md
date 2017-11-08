# Selenium WebDriver (Java) for NeoLoad

## Overview

The Selenium WebDriver (java) allows you to use a Selenium script to create or update NeoLoad User Paths and/or measure
 the end user experience on one or a few browsers or mobile devices and to send these measurements to NeoLoad using the
 Data Exchange API.

Tested wrapped drivers include browser drivers (ChromeDriver, FirefoxDriver…) and the Perfecto driver.

| Property          | Value             |
| ----------------    | ----------------   |
| Maturity           | Stable|
| Author             | Neotys Partner Team |
| License           | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad         | 5.3 (Enterprise or Professional Edition w/ Integration & Advanced Usage and NeoLoad Web option required)|
| Requirements | Java Client for Data Exchange API and Java Client for Design API (See &lt;NeoLoad install folder&gt;/api).|
| Bundled in NeoLoad | No |
| Download Binaries    | See the [latest release](https://github.com/Neotys-Labs/Selenium-WebDriver-Java/releases/latest)

## NEW in v2.2.0: Automatic Transactions
Beyond the requisite use of 'NLWebDriverFactory.newNLWebDriver' to wrap your existing driver, there's nothing you need to
 do to your existing Selenium scripts for traffic to be captured in logical NeoLoad transaction containers. Navigation events
 such as 'driver.get(...)'  and other interactive events will automatically translate to 'Navigate to: ...' and 'Click: ...'
 containers. Prior to this, in order to create usable/readable NeoLoad user paths, Selenium scripts had to be augmented
 with calls to 'driver.startTransaction(...)'. This is no longer a hard requirement.

If you need to align container names to key business events (like Login, Checkout, etc.) that span across DevOps toolchains,
 you can still explicitly call 'driver.startTransaction', scoped by Runnable blocks or trailing standard WebDriver calls.

## NEW in v2.2.0: Fluent API
By calling 'driver.fluent()', you now have a transaction-aware fluent way to write Selenium scripts that require no
 'Thread.sleep(...)' calls! Arbitrary sleep calls are a common source of flaky, slow-running tests. With the new '.fluent()'
 method, regardless of what mode the NLWebDriver is running in, scripts take the minimum time they need to run.

## Release notes and change log
For more information on the latest updates, see the [changeLog.txt](/Neotys-Labs/Selenium-WebDriver-Java/blob/develop/changeLog.txt)

## Documentation
Read the [Integration with Selenium](https://www.neotys.com/documents/doc/neoload/latest/en/html/#8266.htm).







 

