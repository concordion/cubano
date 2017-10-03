# Cubano WebDriver Manager

Provides a set of 'managed' browser providers that will automatically download and start the driver required for the browser your test is targeting. 

# Configuration

All configuration is placed in the test project's config.properties or user.properties files (see cubano-config for more details) on these files.

## Bonigarcia WebDriver Manager

The automatic Selenium WebDriver binaries management functionality is provided by the [https://github.com/bonigarcia/webdrivermanager](Bonigarcia WebDriver Manager) GitHub project.

If proxy settings have been configured for the project, WebDriver Manager will be configured to use the proxy settings.

WebDriver manager supports a number of settings, such as the path to download drivers to.  Any settings in the configuration files starting with 'wdm.' will be passed to WebDriver Manager.

## Selenium WebDriver Configuration

##### webdriver.browserProvider

The fully qualified name of the browser provider class, if the package is not included then the supplied value will be prefixed with "org.concordion.cubano.driver.web.provider."
* This may be overridden by setting the system property browserProvider
* Will fail if not supplied

##### webdriver.resartBrowserTestCount

* Not yet supported
* If set to any value greater than zero will cause the browser to restart after that many tests have completed
* Defaults to 0

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

##### proxy.host

The hostname, or address, of the proxy server.

Will be populated from the following locations in this order:
1. user.properties file proxy.host setting
1. config.properties file proxy.host setting 
1. System property http.proxyHost
1. Environment variable HTTP_PROXY 

##### proxy.port

The port number of the proxy server.

Will be populated from the following locations in this order:
1. user.properties file proxy.port setting
1. config.properties file proxy.port setting 
1. System property http.proxyPort
1. Environment variable HTTP_PROXY 
1. Default to port 80

##### proxy.username

Username for proxy authentication, for a NTLM proxy the username can be in the format &lt;domain&gt;\&lt;username&gt;

Will be populated from the following locations in this order:
1. user.properties file proxy.username setting
1. config.properties file proxy.username setting 
1. System property http.proxyUser
1. Environment variable HTTP_PROXY 
1. Environment variable HTTP_PROXY_USER

##### proxy.password

Password for proxy authentication.

Will be populated from the following locations in this order:
1. user.properties file proxy.password setting
1. config.properties file proxy.password setting 
1. System property http.proxyPassword
1. Environment variable HTTP_PROXY 
1. Environment variable HTTP_PROXY_PASS

##### proxy.nonProxyHosts

Indicates the hosts that should be accessed without going through the proxy. Typically this defines internal hosts. The value of this property is a list of hosts, separated by the ',' character. In addition the wildcard character '*' can be used for pattern matching. For example proxy.nonProxyHosts="*.foo.com,localhost" will indicate that every hosts in the foo.com domain and the localhost should be accessed directly even if a proxy server is specified.

Will be populated from the following locations in this order:
1. user.properties file proxy.nonProxyHosts setting
1. config.properties file proxy.nonProxyHosts setting 
1. System property http.nonProxyHosts (Note: uses | rather than , to separate hosts and will be automatically converted) 
1. Environment variable NO_PROXY
1. Default to "localhost,127.0.0.1"


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
* &lt;custom&gt;: name of any other profile you have configured in firefox - it must exist
* &lt;path&gt;: directory name of a custom profile: it must exist

If you are using/creating a profile this can be further customized using:
* firefox.profile[any.valid.profile.setting] = value

WARNING: At present if a profile is created or used when using the gecko / marionette driver then it suffers from a memory leak. See https://github.com/mozilla/geckodriver/issues/983 and https://stackoverflow.com/questions/46503366/firefox-memory-leak-using-selenium-3-and-firefoxprofile 



### Internet Explorer


### Opera


### Phantom JS