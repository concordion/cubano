# Cubano WebDriver Manager

Provides a set of 'managed' browser providers that will automatically download and start the driver required for the browser your test is targeting.

The automatic Selenium WebDriver binaries management functionality is provided by the [https://github.com/bonigarcia/webdrivermanager](Bonigarcia WebDriver Manager) GitHub project.


# Configuration

All configuration is placed in the test project's config.properties or user.properties files (see cubano-config for more details) on these files.

Cubano will pick default settings where it can, but if you're behind a proxy then you'll need to supply the proxy settings if you wish to have the browser drivers automatically downloaded.


## Bonigarcia WebDriver Manager Configuration

If proxy settings have been configured for the project, WebDriver Manager will be configured to use the proxy settings.

WebDriver manager supports a number of settings to customise its behaviour. Any settings in the configuration files starting with 'wdm.' will be passed to WebDriver Manager, documentation for these settings can be found at [https://github.com/bonigarcia/webdrivermanager]( - ).

Some recommend settings for use in your project, particularly if you're on a corporate network where your user files end up on the network rather than your PC are:

    wdm.targetPath=/path/to/keep/WebDriverManager

If you do not wish to have the drivers downloaded and/or updated automatically you will need to set and place the required driver files in 'wdm.targetPath' yourself

    wdm.forceCache=true


## Selenium WebDriver Configuration

These settings apply to all browsers.

##### webdriver.browserProvider

The fully qualified name of the browser provider class, if using one of the built in provider classes the package name is not required as it will default to "org.concordion.cubano.driver.web.provider".
* This may be overridden by setting the system property browserProvider

*Local browser options:*
* ChromeBrowserProvider
* ChromeHeadlessBrowserProvider (TODO NOT YET DEVELOPED)
* EdgeBrowserProvider
* FirefoxBrowserProvider (default if setting not supplied)
* InternetExplorerBrowserProvider
* OperaBrowserProvider
* SafariBrowserProvider
    
These have been chosen as they are the most commonly used browsers and are supported by the Bonigarcia WebDriver Manager and will automatically download the driver executable required to drive the associated browser. 

If you wish to use an alternative browser you will need to download the browser driver and create a new class implementing the BrowserProvider interface.
    
*Local Selenium Grid:*

* SeleniumGridBrowserProvider (NOT YET DEVELOPED)
        
*Remote Selenium Grid:*

* BrowserStackBrowserProvider (NEEDS WORK)
* SauceLabsBrowserProvider (NEEDS WORK)


##### webdriver.browser.position

Specify a custom window location for browser in the format &lt;X&gt;x&lt;Y&gt;, eg 1x1

##### webdriver.browser.dimension

Specify a custom window size for browser in the format &lt;width&gt;x&lt;height&gt;, eg 192x192

##### webdriver.browser.maximized

If set to true will maximize the browser when it is first opened 
* Defaults to false, allowed values are true or false

##### webdriver.event.logging

If set to true will log all the actions the tests are making to Selenium WebDriver using  
* Defaults to true, allowed values are true or false

##### webdriver.timeouts.implicitlywait

If you wish to use implicit rather than explicit waits then configure this value
* Defaults to 0
* Refer to the documentation on Cubano's BasePageObject for more information on this setting
TODO Add documentation to BasePageObject on [Yandex HtmlElements](https://github.com/yandex-qatools/htmlelements) 
* WARNING: Do not mix with Selenium WebDriver's implicit or explicit waits as the timeout behaviour becomes unpredictable.

##### proxy.required

If true then the proxy will be set on the requested browser
* Defaults to false, valid options are true or false

See cubano-config for more proxy settings.


## Browser Specific Configuration

### Chrome

Documentation for the various [ChromeDriver](https://github.com/SeleniumHQ/selenium/wiki/ChromeDriver) options is at [https://sites.google.com/a/chromium.org/chromedriver/capabilities]( - ).

##### chrome.argument.&lt;number&gt;

"number" is meaningless but must be unique. The value must be a valid chrome argument.

A full list can be found here: https://peter.sh/experiments/chromium-command-line-switches/

For example, to prevent the popup "Chrome is being controlled by automated test software" from appearing when running automated tests use this: 

    chrome.argument.1 = disable-infobars
		
##### chrome.capability.&lt;any.valid.capability&gt;

Set desired capabilities.

##### chrome.extension.&lt;number&gt;

"number" is meaningless but must be unique. The path must point to a valid file

If the path contains "%PROJECT%" it will be replaced with root folder of project

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

Options for the various [FirefoxDriver](https://github.com/SeleniumHQ/selenium/wiki/FirefoxDriver) settings are at TODO ????

#### Firefox Portable

[Firefox Portable](https://portableapps.com/apps/internet/firefox_portable) can also be used and is a great choice if you need a different version than your installed Firefox version for any reason. 

As with Firefox, any versions prior to 48 will need the legacy flag set to true, see configuration section below for more details. Note: FirefoxPortable 47 has a bug an will not work with Selenium WebDriver. FirefoxPortable 47.0.1 works fine.
  
You will need to specify the location of the executable using the firefox.exe configuration property:

* In some corporate environments you may have to rename the executable to get around polices that block FirefoxPortable.exe.
* From version 48 onwards (ie when using the marionette/gecko driver) make sure you are pointing to "\path\to\FirefoxPortable\App\Firefox64\firefox.exe" and not just "\path\to\FirefoxPortable\FirefoxPortable.exe"

You'll may also want to configure Firefox Portable to run any time, regardless of how many other instances of Firefox or FirefoxPortable that are running:
  
1. Copy FirefoxPortable.ini from FirefoxPortable\Other\Source to FirefoxPortable\
1. Edit FirefoxPortable.ini and change AllowMultipleInstances=false to AllowMultipleInstances=true

 
#### Configuration

##### firefox.useLegacyDriver

Up to version 47, the driver used to automate Firefox was an extension included with each client. 

Marionette is the new driver that is shipped/included with Firefox. This driver has it's own protocol which is not directly compatible with the Selenium/WebDriver protocol.

The Gecko driver (previously named wires) is an application server implementing the Selenium/WebDriver protocol. It translates the Selenium commands and forwards them to the Marionette driver.

For older browsers (version 47 and below) set this property to true, for newer browsers set this to false (default).

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

##### firefox.extension.&lt;number&gt;

"number" is meaningless but must be unique. The path must point to a valid file

If the path contains "%PROJECT%" it will be replaced with root folder of project


### Internet Explorer

Options for the various [InternetExplorerDriver](https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver) settings are at TODO ????

##### ie.capability.&lt;any.valid.capability&gt;

Sets capabilities

### Opera

##### opera.capability.&lt;any.valid.capability&gt;

##### opera.exe

Specify the location of browser if your firefox installation path is not automatically discoverable, eg:
* %USERPROFILE%/Documents/Mozilla FireFox Portable/FirefoxPortable.exe


### Safari

Unlike the other browsers, Safari 10 and above come with built-in [WebDriver support](https://webkit.org/blog/6900/webdriver-support-in-safari-10/). To use the Safari driver you need to configure Safari to allow automation. As a feature intended for developers, Safari’s WebDriver support is turned off by default. To turn on WebDriver support, do the following:

1. Ensure that the Develop menu is available. It can be turned on by opening Safari preferences (Safari > Preferences in the menu bar), going to the Advanced tab, and ensuring that the Show Develop menu in menu bar checkbox is checked.

1. Enable Remote Automation in the Develop menu. This is toggled via Develop > Allow Remote Automation in the menu bar.

1. The documentation states that you will also need to "authorize safaridriver to launch the WebDriver service which hosts the local web server. To permit this, run `/usr/bin/safaridriver --enable` once and complete the authentication prompt if it is shown.". However it appears to run just fine without this step.

When using Selenium WebDriver, Safari opens a window that does not get closed when the tests complete.

#### Configuration

Apart from the standard settings (proxy, size, etc) there appears to be very little that can be configured in Safari as per [Getting Started](https://github.com/SeleniumHQ/selenium/wiki/SafariDriver). 

Safari can have problems with the webdriver.browser.postion setting, so use this with care. 

===== option.useTechnologyPreview
Defaults to false

===== option.useCleanSession 
Defaults to false
