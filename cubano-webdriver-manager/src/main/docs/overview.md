# Cubano WebDriver Manager

Provides a set of 'managed' browser providers that will automatically download and start the driver required for the browser your test is targeting. 

# Configuration

All configuration is placed in the test project's config.properties or user.properties files (see cubano-config for more details) on these files.

## Bonigarcia WebDriver Manager

The automatic Selenium WebDriver binaries management functionality is provided by the [https://github.com/bonigarcia/webdrivermanager](Bonigarcia WebDriver Manager) GitHub project.

If proxy settings have been configured for the project, WebDriver Manager will be configured to use the proxy settings.

WebDriver manager supports a number of settings, such as the path to download drivers to.  Any settings in the configuration files starting with 'wdm.' will be passed to WebDriver Manager, documentation for these settings can be found at [https://github.com/bonigarcia/webdrivermanager]( - ).

## Selenium WebDriver Configuration

##### webdriver.browserProvider

The fully qualified name of the browser provider class, if using one of the built in provider classes the package name is not required as it will default to "org.concordion.cubano.driver.web.provider".
* This may be overridden by setting the system property browserProvider

*Local browser options:*
* ChromeBrowserProvider
* EdgeBrowserProvider
* FirefoxBrowserProvider (default)
* InternetExplorerBrowserProvider
* OperaBrowserProvider
* SafarfiBrowserProvider (NOT YET DEVELOPED)
    
These have been choosen as they are the most commonly used browsers and are supported by the Bonigarcia WebDriver Manager and will automatically download the driver executable required to drive the associated browser. 

If you wish to use an alternative browser you will need to download the browser driver and create a new class implementing the BrowserProvider interface.
    
*Local Selenium Grid:*

* SeleniumGridBrowserProvider (NOT YET DEVELOPED)
        
*Remote Selenium Grid:*

* BrowserStackBrowserProvider (NEEDS WORK)
* SauceLabsBrowserProvider (NEEDS WORK)

##### webdriver.browserSize

Specify a custom window size for browser in the format &lt;width&gt;x&lt;height&gt;, eg 192x192

##### webdriver.maximized

If set to true will maximize the browser when it is first opened 
* Defaults to false, allowed values are true or false

##### webdriver.timeouts.implicitlywait

If you wish to use implicit rather than explicit waits then configure this value
* Defaults to 0
* Note: if your project is using Cubano's BasePageObject and the page object pattern Cubano uses [Yandex HtmlElements](https://github.com/yandex-qatools/htmlelements) to populate the fields so that custom page components can be used in place of WebElements.
* WARNING: Do not mix with Selenium WebDriver's implicit or explicit waits as the timeout behaviour becomes unpredictable.

##### proxy.required

If true then the proxy will be set on the requested browser
* Defaults to false, valid options are true or false

See cubano-config for more proxy settings.


## Browser Specific Configuration

### Chrome

Documentation for the various options is at [https://sites.google.com/a/chromium.org/chromedriver/capabilities]( - ).

##### firefox.capability.&lt;any.valid.capability&gt;

Set desired capabilities.

##### chrome.extension.&lt;number&gt;

"number" is meaningless but must be unique. The path must point to a valid file

If the path contains "%PROJECT%" it will be replaced with root folder of project

##### chrome.argument.&lt;number&gt;

"number" is meaningless but must be unique. The value must be a valid chrome argument.

Will always add argument test-type

// More workarounds https://stackoverflow.com/questions/42979877/chrome-browser-org-openqa-selenium-webdriverexception-unknown-error-cannot-get
		options.addArguments("disable-infobars");
		options.addArguments("--disable-popup-blocking");
		

##### chrome.option..&lt;any.valid.option&gt;

Some options you may want to consider:

[https://stackoverflow.com/questions/43797119/failed-to-load-extension-from-popup-box-while-running-selenium-scripts]( - )

	chrome.option.useAutomationExtension = false

##### chrome.preference.&lt;any.valid.preference&gt;

Some preferences you may want to consider:

[https://stackoverflow.com/questions/43797119/failed-to-load-extension-from-popup-box-while-running-selenium-scripts]( - )

	chrome.preference.useAutomationExtension = false




### Edge

##### ie.capability.&lt;any.valid.capability&gt;


### FireFox

##### firefox.useGeckoDriver

Up to version 47, the driver used to automate Firefox was an extension included with each client. 

Marionette is the new driver that is shipped/included with Firefox. This driver has it's own protocol which is not directly compatible with the Selenium/WebDriver protocol.

The Gecko driver (previously named wires) is an application server implementing the Selenium/WebDriver protocol. It translates the Selenium commands and forwards them to the Marionette driver.

For older browsers set this property to false, for newer browsers set this to true (default).

##### firefox.exe

Specify the location of browser if your firefox installation path is not automatically discoverable, eg:
* %USERPROFILE%/Documents/Mozilla FireFox Portable/FirefoxPortable.exe

##### firefox.profile

Specifies the profile name, or full path to a folder, of the firefox profile that you wish to use. There is a good write up on the subject at http://toolsqa.com/selenium-webdriver/custom-firefox-profile.

Values:
* (not specified): will create a new firefox profile using ``new FirefoxProfile()`` which is the recommended behaviour for firefox
* none: no profile will be created 
* default: will use the profile that all users typically use 
    * this will allow you to use any add ons such as firebug and firepath that you may have installed
    * it is a great option for test developers who may use the tests to navigate to a page where they can stop the tests and still have the add-ons they need to inspect the page     
* &lt;custom&gt;: name of any other profile you have configured in firefox - it must exist
* &lt;path&gt;: directory name of a custom profile: it must exist

WARNING: At present if a profile is created or used when using the gecko / marionette driver then it suffers from a memory leak. See https://github.com/mozilla/geckodriver/issues/983 and https://stackoverflow.com/questions/46503366/firefox-memory-leak-using-selenium-3-and-firefoxprofile 

##### firefox.profile.&lt;any.valid.profile.setting&gt;

If a profile has been chosen then firefox preferences can be set, for example:

    # Work around for FireFox not closing, fix comes from here: https://github.com/mozilla/geckodriver/issues/517
    firefox.profile.browser.tabs.remote.autostart = false
    firefox.profile.browser.tabs.remote.autostart.1 = false
    firefox.profile.browser.tabs.remote.autostart.2 = false
    firefox.profile.browser.tabs.remote.force-enable = false

##### firefox.capability.&lt;any.valid.capability&gt;

Sets capabilities, for example:
 
    firefox.capability.acceptSslCerts = false


### Internet Explorer

##### ie.capability.&lt;any.valid.capability&gt;

Sets capabilities

### Opera

##### opera.capability.&lt;any.valid.capability&gt;

##### opera.exe

Specify the location of browser if your firefox installation path is not automatically discoverable, eg:
* %USERPROFILE%/Documents/Mozilla FireFox Portable/FirefoxPortable.exe

