# Configuration

Cubano has a built in configuration management for proving settings to your tests and Cubano components such as the WebDriver Management and API requests.

The configuration utility can read from two files which must be located in the root folder of your project.  These files are:

* *config.properties*: this file should contain all the default setting and must exist and be checked into your projects source control
* *user.properties*: this file contains sensitive information and user specific overrides of settings in config.properties, this file should not be committed to source control

All properties are stored in these files in key = value pairs, for example:

	a.setting = value
	
	
## Environment Setting

The configuration management utility has one required property that must be set in config.properties which is 'environment'.

This allows your properties file to contain settings for multiple environments and to target the current run at a particular environment.  

This setting can be overridden by:
* setting an environment variable also called environment
* setting the value in the user.properties configuration file


## Read Order
Properties can come in two flavors: 
* environment specific - ie prefixed with name of environment, eg 'dev.prop.key = value' 
* default value - ie anything without an envrionment prefix, eg 'prop.key = value'

These values can be also overridden on a per user basis by placing custom settings in a user.properties file.

The search order for looking up a configuration setting is:
1. user.properties file
    1. requested property prefixed with '<environment>.'
    1. requested property 
1. config.properties file
    1. requested property prefixed with '<environment>.'
    1. requested property 

Some settings can also be overridden by the use of system properties and environment variables.  See the documentation for the setting.

  
## Providing An Application Configuration Class

To create application specific settings the recommended approach is to create your own AppConfig class extending rg.concordion.cubano.utils.Config, you'll then be required to implement the loadProperties() method when you can read the properties settings.  You'll need to create getter methods to make these settings available to the test application. 

	
## Supported Setting

##### environment

As documented above

##### proxy.required

If true then the proxy will be set on the requested browser
* Defaults to false, valid options are true or false

##### proxy.host

The hostname, or address, of the proxy server.

Will be populated from the following locations in this order:
1. config.properties and user.properties file proxy.host setting
1. System property http.proxyHost
1. Environment variable HTTP_PROXY 
    Format is http://[username:password@]proxy.thing.com:8080

The remaining proxy settings listed below will only be retrieved from the same source the host setting was found.

##### proxy.port

The port number of the proxy server.

Depending on where you specified the proxy host, the port will be retrieved from one of these sources:
1. user.properties file proxy.port setting
1. config.properties file proxy.port setting 
1. System property http.proxyPort
1. Environment variable HTTP_PROXY 
1. Default to port 80

##### proxy.username

Username for proxy authentication, for a NTLM proxy the username can be in the format &lt;domain&gt;\&lt;username&gt;

Depending on where you specified the proxy host, the username will be retrieved from one of these sources:
1. user.properties file proxy.username setting
1. config.properties file proxy.username setting 
1. System property http.proxyUser
1. Environment variable HTTP_PROXY 
1. Environment variable HTTP_PROXY_USER

##### proxy.password

Password for proxy authentication.

Depending on where you specified the proxy host, the password will be retrieved from one of these sources:
1. user.properties file proxy.password setting
1. config.properties file proxy.password setting 
1. System property http.proxyPassword
1. Environment variable HTTP_PROXY 
1. Environment variable HTTP_PROXY_PASS

##### proxy.nonProxyHosts

Indicates the hosts that should be accessed without going through the proxy. Typically this defines internal hosts. The value of this property is a list of hosts, separated by the ',' character. In addition the wildcard character '*' can be used for pattern matching. For example proxy.nonProxyHosts="*.foo.com,localhost" will indicate that every hosts in the foo.com domain and the localhost should be accessed directly even if a proxy server is specified.

Depending on where you specified the proxy host, the non proxy hosts list will be retrieved from one of these sources:
1. user.properties file proxy.nonProxyHosts setting
1. config.properties file proxy.nonProxyHosts setting 
1. System property http.nonProxyHosts (Note: uses | rather than , to separate hosts and will be automatically converted) 
1. Environment variable NO_PROXY

