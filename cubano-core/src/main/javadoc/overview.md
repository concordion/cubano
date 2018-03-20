The test automation framework is a Java based tool that provides a framework for writing living documentation that can drive the system under test - whether that system runs in a browser or is a service.

# Core Third Party Components

* <a href="http://concordion.org" target="_blank">Concordion</a>: open source tool for automating living documentation (aka Specification by Example, Acceptance Test Driven Development, etc)
* <a href="http://www.seleniumhq.org/projects/webdriver" target="_blank">Selenium WebDriver</a>: an API for automating user actions on a browser
* <a href="https://github.com/yandex-qatools/htmlelements" target="_blank">Yandex HtmlElements</a>: a framework providing easy-to-use way of interaction with web-page elements
* <a href="http://www.slf4j.org/" target="_blank">SLF4J</a> and <a href="http://logback.qos.ch/" target="_blank">Logback</a>: logging framework 
* <a href="http://junit.org" target="_blank">Junit</a>: a simple framework to write repeatable tests, it is an instance of the xUnit architecture for unit testing frameworks
* <a href="http://gradle.org" target="_blank">Gradle</a>: build and dependency management tool, this can be replaced with Maven if you're already using Maven and don't want two different build tools 


# Writing the Specifications and Tests
This framework uses Concordion to produce living documentation as it gives a lot of flexibility in formatting the specification over tools like <a href="https://cucumber.io" target="_blank">Cucumber</a>.

There are books written on the subject of what makes a good specification so I'm not going to attempt to cover that here, I will point you to a couple of good resources:

* <a href="https://books.gojko.net/specification-by-example" target="_blank">Specification by Example: How Successful Teams Deliver the Right Software by Gojko Adzic</a> - the bible for Specification by Example
* <a href="http://concordion.org/technique/java/markdown/" target="_blank">Concordion's Hints and Tips</a>

When developing the test suite there are a few goals we want to achieve:

* Readable: tests should be easily understood and allow for the fact that it will have different audiences with different needs: some just want an overview of what the system does, others will want a more technical understanding and others need to know exactly what the test did to diagnose issues
* Robust: a screen change in the application under test should not mean the test suite has to be changed in multiple places
 
To meet these goals there are a few patterns that we will need to use, these have been documented below.  The patterns all follow a theme, and that theme is _abstraction_.

## Specification
Concordion supports writing a specification in both <a href="http://concordion.org/instrumenting/java/html/" target="_blank">HTML</a> and <a href="http://concordion.org/instrumenting/java/markdown/" target="_blank">Markdown</a>.  I recommend markdown as it is much quicker to write than html and non developers generally will pick it up quicker.

Avoid writing tests that read like scripts and don't get to involved in the implementation details of the system under test.  These lead to specifications that require changing as the system under test is changed.  

Avoid copying code into tests, eg if asserting that an error is displayed just assert that the element is visible rather checking the contents of the message as this may change at any time leading to brittle tests - unless the content of the message is important and worth validating.

Aim for a level of abstraction away from the implementation of the system under test.

## Test
Concordion runs on top of JUnit but you'll find that your test 'fixtures' (the class your specification uses) don't look anything like a standard JUnit test suite.  

The code in here should not contain any system implementation details and provides another level of abstraction.

## Driver
The driver is the bit that talks to the system under test.  This could be a PageObject for a Web Application or a class to call the Web Services the system under test exposes.  It could also be wrapper around the database or email system.

The objective behind these classes is to group related action is once place so that if the system under test changes then there is only place to fix and test code to reflect that change. 

## Workflow
Often there are repeated actions that the tests perform, such as logging into an application.  These can get abstracted out into a workflow class to avoid repeating the same sequence of actions in all your tests. 


# Automating Web Applications
This framework uses Selenium WebDriver and recommends abstracting the specifics of system under test away from the test using PageObjects. 

These patterns are widely written about and there are many resources available on the internet.  For developers and testers new to test automation I highly recommend looking at these two resources from Dave Haeffner:

1. <a href="https://seleniumguidebook.com" target="_blank">The Selenium Guidebook</a>: A guidebook into all things selenium including a guide to testers on what java they will need to learn 
1. <a href="http://elementalselenium.com" target="_blank">Elemental Selenium</a>: A weekly newsletter with different tips

While there are other patterns and approaches to test automation, the following patterns are those I've found to be most useful and can be developed with the minimum amount of effort and skill level.

## Page Object Pattern
The <a href="http://code.google.com/p/selenium/wiki/PageObjects" target="_blank">Page Object Pattern</a> is a pattern that abstracts a web page's UI into a reusable class. In addition to UI, functionality of the page is also described in this class. This provides a bridge between page and test.

The idea is to create a level of abstraction to separate the tests from the system under test, and to provide a simple interface to the elements on the page. Here are the main advantages of Page Object Pattern using:

* Centralised UI coupling - one place to make changes around the UI
* Simple and clear tests (a readable DSL)
* Code reuse
* Easy creation of new tests

Further Reading: <a href="http://martinfowler.com/bliki/PageObject.html" target="_blank">martinfowler.com/bliki/PageObject.html</a>

_Example of a login page object:_

```java
	public class LoginPage extends PageObject<LoginPage>  {
		// @FindBy not required as unannotated WebElement will default 
		// to matching the field name against id and name attributes 
		WebElement username;
		WebElement password;
		
		{@literal @FindBy(id = "log_in")}
		WebElement loginButton;
	
		public BpmLoginPage(TestDriveable test) {
			super(test);
		}
	
		{@literal @Override}
		protected ExpectedCondition<?> pageIsLoaded(Object... params) {
			return ExpectedConditions.visibilityOf(loginButton);
		}
	
		public HomePage loginAs(User user) {
			if (user != null){
				this.username.sendKeys(user.getUsername());
				this.password.sendKeys(user.getPassword());
			}
			
			return navigateUsing(loginButton, HomePage.class);
		}
	}
```

## Page Component Pattern
With single page applications and more advanced web applications with complex web controls the standard page object pattern can struggle.  As the test suite grows we may need to introduce other techniques to keep it maintainable and this is where the page component pattern fits in.

Page Components are classes that represent sections (header, footer, navigation bar), or complex controls, they are just like page objects... but littler.

We've included the open source project '<a href="https://github.com/yandex-qatools/htmlelements" target="_blank">Yandex HtmlElements</a>' that makes creating and using these components in a page object as simple as using any standard web element.

_Example of a simple component that would be used in place of the standard WebElement for a Dojo based WebApplication:_ 

	public class DijitWebElement extends HtmlElement {
		{@literal @FindBy(xpath = ".//input[contains(@class, 'dijitInputInner')]")}
		WebElement input;
		
		{@literal @Override}
		public String getText() {
			return input.getAttribute("value");
		}
		
		{@literal @Override}
		public void sendKeys(CharSequence... charSequences) {
			input.sendKeys(charSequences);
		}
	}

## Page Factory Pattern
In order to support the PageObject pattern, WebDriver's support library contains a factory class where we use the annotation @FindBy for making the WebElement object to know to which element it belongs to on web page. By using this pattern we need not to write the code for finding each and every Web Element.

This functionality is built into the framework, the only requirement is that you page objects inherit off BasePageObject - although I recommend you have an additional layer called PageObject so you can put any application specific custom functionality in this layer.  

## Fluent Pattern  
The idea behind a Fluent interface is that one can apply multiple properties to an object by connecting them with dots, without having to re-specify the object each time.

This applies to all reusable objects, not just page objects and probably should be highlighted earlier but for developers and testers new to this approach need to understand the above patterns first.

_Example of a test calling page objects implementing the fluent pattern:_

	{@literal @Test}
	public void newLodgementPopulatesSummaryPage() {
		SummaryPage summaryPage = LoginPage
			.visit(driver)
			.fillDefaultUser()
			.login()
	
			.fillNewLodgementFields(data)
			.clickNextButton()
	
			.fillDeclaration(data);
	
		assertThat("Summary Page is populated correctly", summaryPage.checkData(data),  is(""));
	}   

## Beyond The Page Object Pattern
I haven't used it, but I keep coming across the Screenplay Pattern (formerly known as the <a href="http://www.slideshare.net/RiverGlide/a-journey-beyond-the-page-object-pattern"  target="_blank">Journey Pattern</a>). 

Further Reading:

* https://ideas.riverglide.com/page-objects-refactored-12ec3541990#.cvqm92cpv
* https://fasterchaos.svbtle.com/journey-pattern
* https://confengine.com/selenium-conf-2016/proposal/2475/the-trouble-with-page-objects-things-you-always-knew-were-wrong-but-couldnt-explain-why

# Automating Services (REST / SOAP / etc)

## REST
This framework includes {@link org.concordion.cubano.driver.http.HttpEasy}, a fluent wrapper built around HttpUrlConnection that can easily send and receive REST messages.

## SOAP
This framework includes {@link org.concordion.cubano.driver.http.HttpEasy}, a fluent wrapper built around HttpUrlConnection that can easily send and receive SOAP messages - if you have the soap message in an xml document.  

Alternatives:

* wsimport
* SOAP with Attachments API for Java (SAAJ)
* Java API for XML Web Services (JAX-WS)
* Apache Camel 

## Other
You could use Apache Camel to do it in a standardised way. Camel will help you to:
* Consume data from any source/format
* Process this data
* Output data to any source/format

Apache Camel is developed with <a href="https://en.wikipedia.org/wiki/Enterprise_application_integration"  target="_blank">Enterprise Integration Patterns</a>.

There are some <a href="http://tools.jboss.org/features/apachecamel.html" target="_blank">eclipse plugins</a> that offer drag and drop support for building and testing.


# Automating Database Requests
There are a range of options:

**Low Level:**
* JDBC: will meet your needs but needs some work to get going

**Lightweight:** 

My personal preference would be to use either of these libraries for the flexibility they add with named parameter support and POJO mapping.  They get you up and running immediately 
and are simpler than JDBC.

* <a href="http://zsoltherpai.github.io/fluent-jdbc/" target="_blank">Fluent JDBC</a>: is a java library for operating with SQL queries conveniently
* <a href="http://jdbi.org/" target="_blank">JDBI</a>: a SQL convenience library for Java

**Other:**
Generally a full ORM package such as Hibernate or JPA will be overkill for your needs.  <a href="http://www.jooq.org/" target="_blank">jOOQ</a> looks like an interesting alternative but is still overkill for most testing requirements in my opinion.  


# Framework
## Configuration Settings
The framework comes with a Config class which scans config.properties for the properties the framework exposes.  For applications specific properties create an AppConfig class that extends off config and expose these via that class.  

The tool allows properties to be overridden in certain cases so will look up a property by prepending the values below in the order specified until a value is found:
1. <username>
1. <environment>
1. 'default' (actually has no prefix)

## General Settings
| Setting                               | Description                                                                        |
| :------------------------------------- | :---------------------------------------------------------------------------------- |
| environment                           | Specifies which environment to use get application specific properties for.<br/>NOTE: First looks to see if the system property 'environment' has been set before looking through 
this configuration file for a value.                                |
| **WebDriver** ||
| webdriver.browser                     | browser to test against<br/>local: FireFox, Chrome, IE</br>remote: as above but add browser version, see the BrowserStack class for list of supported browsers/devices |
| webdriver.defaultTimeout              | default timeout value to use on element look ups  |
| webdriver.browserSize                 | specify a custom window size for browser, if not specified is maximised |
| webdriver.{@literal <browser>}.exe               | location of browser, eg: %USERPROFILE%/Documents/Mozilla FireFox Portable/FirefoxPortable.exe |
| webdriver.{@literal <browser>}.activatePlugins	| If value is 'true' will add FireBug and FirePath plugins (only supported for FireFox currently)<br/>WARNING: do not activate this by default - it should not be enabled on CI server as it is only for use by the test developers</br>To update plugins goto below locations and download to libs project folder:<ul><li><a href="https://addons.mozilla.org/en-US/firefox/addon/firebug" target="_blank">Firebug</a></li><li><a href="https://addons.mozilla.org/en-US/firefox/addon/firepath" target="_blank">FirePath</a></li></ul> | 
| webdriver.timeouts.implicitlywait     | If choosing to use implicit waits and not use implicit waits then consider using @timeout provided by Yandex HtmlElements.  Setting this value will set a global default wait period on all WebElements.  (defaults to zero) which can be overridden by the @timeout annotation.<br/>WARNING: Do not mix with Selenium WebDriver's implicit or explicit waits as the timeout behaviour becomes unpredictable. |


## HttpEasy
{@link org.concordion.cubano.driver.http.HttpEasy}: provides a fluent style wrapper around HttpURLConnection.  It can:

* handle most HTTP methods (GET, POST, HEAD, etc)
* upload and download files
* perform REST and SOAP requests
* handle corporate proxies 

## ActionWait
{@link org.concordion.cubano.driver.action.ActionWait}: Similar to Selenium's {@link org.openqa.selenium.support.ui.FluentWait} implementation but designed for long running tasks such as querying a database until some data appears and unlike FluentWait it handles exceptions other than RuntimeExceptions.
       
## ActionTimer
{@link org.concordion.cubano.driver.action.ActionTimer}: a simple utility for logging duration of a task

## Exception Handling
As a general rule of thumb - don't!

The framework will catch and log all exceptions, so only catch an exception if you need to perform an special action in the event an exception is thrown.

## Logging
The framework uses the <a href="https://github.com/concordion/concordion-logback-extension" target="_blank">Concordion Logging extension</a> to keep a separate log per test and a link to the log is added to the bottom right of the page.  

Actual implementation is a combination of <a href="http://www.slf4j.org/" target="_blank">SLF4J</a> and <a href="http://logback.qos.ch/" target="_blank">Logback</a>. 

	import org.slf4j.Logger;
	import org.slf4j.LoggerFactory;
	
	public abstract class YourClass {
		private static final Logger logger = LoggerFactory.getLogger(YourClass.class);
		
		...
	}

_Do not use System.out.println() or java.util.logging._
  
### Log level usage:

* Exception: are automatically caught and logged so you do not need to explicitly handle those
* Warning: use as needed
* Info: should only be used for very high level concepts so that they don't show up on the Jenkins console, I try to keep that to a minimum
* Debug: should use for test steps eg going to a page
* Trace: should use for finer level eg filling field on a page

## Storyboard
The framework uses the <a href="https://github.com/concordion/concordion-storyboard-extension" target="_blank">Concordion Storyboard extension</a> to record the steps taken to perform the test, such as screenshots and soap requests and response data.  

This serves several purposes:
* It is a nice addition to the documentation
* It is a fantastic debugging tool

If you use the navigateUsing() method in your page objects when an element is about to be clicked that performs a page navigation then screenshots will be added to the storyboard automatically, eg `navigateUsing(loginButton)`. This has the added advantage that way if any actions have been performed on the screen (eg data entered) then those details are available in the screenshots. 

## HtmlElement
The framework includes <a href="https://github.com/yandex-qatools/htmlelements" target="_blank">Yandex HtmlElements</a> as means of providing easy-to-use way of breaking complex web pages into page components that also works seamlessly with the page factory.

We also provide a couple of interfaces that your component can implement that allow it to work with the framework more easily:

* {@link org.concordion.cubano.driver.web.pagefactory.WebDriverAware}: will supply the WebDriver to the component
* {@link org.concordion.cubano.driver.web.pagefactory.PageObjectAware}: will supply page object the component is on to the component 

# Guidelines
These are general recommendations rather than hard and fast rules.

## Coding Standards
Treat test code as a firsts class citizen - it needs the same level of code quality as your application code otherwise tests will become unmaintainable.

## Configuration
Tests must be environment independent in that they must be able run in any environment (with the possible exception of production) by only changing the URL the tests are running against. Plan for this by:
* have a configuration file for environment settings
* having tests validate require data exists and set it up if its missing
* DO NOT have tests use manually setup data - it won't exist in the next environment

## Run Tests in Parallel
Eventually the test suite is going to take too long to run, and tests will need to run in parallel.  I suggest we should start out doing this straight away so any issues are caught early - it can take a very long time to refactor existing tests if you do this down the track.  This will require Selenium Grid.

## Fail Fast
If something fails, it's usually a good idea to fail fast and exit the specific test immediately rather than allow the test to muddle along.

## Test Inheritance
Tests inheriting from or calling other test should be kept to a minimum, in my experience this leads to hard to understand tests and a fragile test suite that is not easily modified.  

## Asserts
Asserts belong in the specification and not the tests.

## What to Test
Don't try to automate everything, there needs to be a return on investment.  If it's going to take too long to develop maybe it should remain a manual step.  Functional tests are one component in the stack of: unit, functional, exploratory (manual), and performance testing.

Test pyramid <a href ="http://martinfowler.com/bliki/TestPyramid.html" target="_blank">http://martinfowler.com/bliki/TestPyramid.html</a>

## Exceptions vs Failures
An exception should prevent running any further tests in the spec, a failure will allow the rest of the spec to complete.  In Concordion this is implemented by the @FailFast annotation.


# How To...
## Choose your browser
For developing tests, use whatever browser works for you... or go with FireFox.  It is the industry standard for developing automated test suites in WebDriver and Selenium WebDriver comes with support for FireFox out of the box.  Firefox also has a couple of great plugins (FireBug and FirePath) that make finding and testing element locators a breeze.  

For running tests in your CI server then this will depend on the project.  Are you developing in house for a specific browser and version?  Is it a public web application running on multiple browsers and devices?  This framework can run either local desktop browsers with support out of the box for Firefox, Chrome and Internet Explorer, or on a remote Selenium Grid and supports BrowserStack or SauceLabs.

## Create a PageObject
When starting with a new page you're going to want to create a page object as your first step.  

Navigate to the page and use a combination of FireBug and FirePath Firefox plugins (or use the inbuilt Developer Tools) to find the locaters for the WebElements you wish to use. This can be rather tedious on a page with lots of elements but there is not a lot of options:

1. Right click on an element and select 'Inspect Element'
1. On highlighted html in FireBug right click and select 'Copy Unique Selector'
1. Use this value in your PageObject
1. Use FirePath to validate your CSS selector

Other optsions:
* Use the test suit to navigate to the page you're after.  
* Develop a page object generator using WebDriver to parse the page and generate an initial at a page object.

## Use a selector for page elements
Generally the preferred method of finding elements on a page is by their Id, if that's not available then use a CSS selector and finally XPath if all else fails.

If using CSS / XPath don't specify parent controls if it can be helped, this has the following benefits:

* Less brittle - the page object should withstand some page refactoring without breaking 
* Readability

If the id / class name that you are selecting on is not unique then put the minimum number of parents that you can.
Examples of CSS selectors can be found here: 

* http://www.w3schools.com/cssref/css_selectors.asp 
* http://www.guru99.com/using-contains-sbiling-ancestor-to-find-element-in-selenium.html

It's worth considering wrapping the complex web controls or section of pages that are repeated across other pages as page components. 

## Wait for a page element
_NEVER_ use thread.sleep().  Ever!

_DO NOT_ use WebDrivers implicit wait feature - it's too broad reaching for our needs and doesn't play well with explicit waits.

_DO_ use WebDriveWait, FluentWait 

_DO_ use @timeout field annotation:

* this performs an implicit wait on any element it's applied to
* don't apply it to an element that you are also performing and explicit wait on  

## Wait for anything else
_NEVER_ use thread.sleep().  Ever!

Use the frameworks built in {@link org.concordion.cubano.driver.action.ActionWait} utility.
