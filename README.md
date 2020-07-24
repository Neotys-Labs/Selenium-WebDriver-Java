# Selenium WebDriver (Java) for NeoLoad

## Overview

The Selenium WebDriver (java) allows you to use a Selenium script to create or update NeoLoad User Paths and/or measure the end user experience on one or a few browsers or mobile devices and to send these measurements to NeoLoad using the Data Exchange API.

Tested wrapped drivers include browser drivers (ChromeDriver, FirefoxDriver…) and the Perfecto driver.

| Property          | Value             |
| ----------------    | ----------------   |
| Maturity           | Stable|
| Author             | Neotys |
| License           | [BSD Simplified](https://www.neotys.com/documents/legal/bsd-neotys.txt) |
| NeoLoad         | 5.3 (Enterprise or Professional Edition w/ Integration & Advanced Usage and NeoLoad Web option required)|
| Requirements | Java Client for Data Exchange API and Java Client for Design API (See &lt;NeoLoad install folder&gt;/api).|
| Bundled in NeoLoad | No |
| Download Binaries    | See the [latest release](https://github.com/Neotys-Labs/Selenium-WebDriver-Java/releases/latest)

## Documentation
Read the [Integration with Selenium](https://www.neotys.com/documents/doc/neoload/latest/en/html/#8266.htm).

## Changelog

- Version 3.0.0 (July 24, 2020)
    - IMPROVED: Update method addProxyCapabilitiesIfNecessary for latest selenium version.
    - FIXED: Negative response time retrieved when page loaded without the "loadEventStart" event sent.
    - IMPROVED: Review exception handling for Design API Client manager.

- Version 2.1.3 (November 16, 2017)
    - IMPROVED: Update version of design api client.
  
- Version 2.1.2 (November 15, 2017)
    - IMPROVED: Update version of perfecto advanced action.
    
- Version 2.1.1 (October 4, 2017)
    - FIXED: Return cause of InvocationTargetException instead of InvocationTargetException when wrapping selenium methods.

- Version 2.1.0 (June 14, 2017)
    - IMPROVED: Improved End User Experience: add the possibility to specify UserPath, Transaction and automatically send timers for Transactions.

- Version 2.0.0 (November 07, 2016)
    - IMPROVED: Add the possibility to use NeoLoad Selenium Proxy during a NeoLoad recording and to set Transaction names with method StartTransaction.

- Version 1.2.0 (April 21, 2016)
    - IMPROVED: Perfecto results are automatically downloaded, parsed, and sent to NeoLoad when the Perfecto remote web driver is used.

- Version 1.1.1 (Mar 18, 2016)
    - FIXED: There was a compatibility issue with Vaadin testbench. (#9399)
    - IMPROVED: Accept arguments such as -Dnl.debug=true as command line arguments as well as environment variables for Sun/Oracle JVMs. (#9242)

- Version 1.1.0 (July 3, 2015)
    - FIXED: An UnsupportedOperationException was thrown when Navigation.to() was called. (#8896)
    - FIXED: The URL and page title associated with a "Path" element was the previous URL and not the current URL.
    - IMPROVED: Navigation timing was added to the data sent to NeoLoad.

- Version 1.0.2 (January 19, 2015)
    - Initial release.






 

