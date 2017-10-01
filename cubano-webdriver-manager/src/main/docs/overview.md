# Cubano WebDriver Manager

Provides a set of 'managed' browser providers that will automatically download and start the driver required for the browser your test is targeting. 

# Configuration

All configuration is placed in the test project's config.properties or user.properties files (see cubano-config for more details) on these files.

## Bonigarcia WebDriver Manager

The automatic Selenium WebDriver binaries management functionality is provided by the [https://github.com/bonigarcia/webdrivermanager](Bonigarcia WebDriver Manager) GitHub project.

If proxy settings have been configured for the project, WebDriver Manager will be configured to use the proxy settings.

WebDriver manager supports a number of settings, such as the path to download drivers to.  Any settings in the configuration files starting with 'wdm.' will be passed to WebDriver Manager.

## Selenium WebDriver Configuration

*webdriver.browserProvider*

The fully qualified name of the browser provider class, if the package is not included then the supplied value will be prefixed with "org.concordion.cubano.driver.web.provider."
* This may be overridden by setting the system property browserProvider
* Will fail if not supplied

*webdriver.resartBrowserTestCount*
* Not yet supported
* If set to any value greater than zero will cause the browser to restart after that many tests have completed
* Defaults to 0

*webdriver.browserSize*

Specify a custom window size for browser in the format &lt;width>x&lt;height>, eg 192x192
* Optional

*webdriver.maximized*

If set to true will maximize the browser when it is first opened 
* Defaults to false, allowed values are true or false

*webdriver.timeouts.implicitlywait*

If you wish to use implicit rather than explicit waits then configure this value
* Defaults to 0
* Note: if your project is using Cubano's BasePageObject and the page object pattern Cubano uses [Yandex HtmlElements](https://github.com/yandex-qatools/htmlelements) to populate the fields so that custom page components can be used in place of WebElements.
* WARNING: Do not mix with Selenium WebDriver's implicit or explicit waits as the timeout behaviour becomes unpredictable.

*proxy.required*

If true then the proxy will be set on the requested browser
* Defaults to false, valid options are true or false

*proxy.address*

Host and port of the proxy in the format &lt;host>:&lt;port>, port can be left off if it is the default port 80

*proxy.username*

Username for proxy authentication, for a NTLM proxy the username con be in the format &lt;domain>\&lt;username>
* Optional

*proxy.password*

Password for proxy authentication
* Optional

## Browser Specific Configuration

### Chrome


### Edge


### FireFox

*firefox.useGeckoDriver*

Up to version 47, the driver used to automate Firefox was an extension included with each client. 

Marionette is the new driver that is shipped/included with Firefox. This driver has it's own protocol which is not directly compatible with the Selenium/WebDriver protocol.

The Gecko driver (previously named wires) is an application server implementing the Selenium/WebDriver protocol. It translates the Selenium commands and forwards them to the Marionette driver.

For older browsers set this property to false, for newer browsers set this to true (default).

*firefox.exe*

Specify the location of browser if your firefox installation path is not automatically discoverable, eg:
* %USERPROFILE%/Documents/Mozilla FireFox Portable/FirefoxPortable.exe

*firefox.profile*

Specifies the profile name, or full path to a folder, of the firefox profile that you wish to use. There is a good write up on the subject at http://toolsqa.com/selenium-webdriver/custom-firefox-profile.

Values:
* Not set: no profile will be created and the tests uses a clean profile without any extra add ons
* new: will create a new firefox profile
* default: will use the profile that all users typically use - this will allow you to use any add ons such as firebug and firepath that you may have installed
* &lt;custom>: name of any other profile you have configured in firefox - it must exist
* &lt;path>: directory name of a custom profile: it must exist

If you are using/creating a profile this can be further customized using:
* firefox.profile[any.valid.profile.setting] = value

WARNING: At present if a profile is created or used when using the gecko / marionette driver then it suffers from a memory leak. 


### Internet Explorer


### Opera


### Phantom JS
