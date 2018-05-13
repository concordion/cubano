[![Build Status](https://img.shields.io/travis/concordion/cubano.svg)](https://travis-ci.org/concordion/cubano)
[![Maven Central](https://img.shields.io/maven-central/v/org.concordion/cubano-concordion.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.concordion%22%20AND%20a%3A%22cubano-concordion%22)
[![Apache License 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# cubano

Cubano is a "ready-made" test framework that provides everything at your fingertips. It's ideal for software delivery teams who want to collaborate around living documentation.

Utilising __Specification by Example__ techniques, the framework implements:

* Web testing (using Selenium WebDriver)
* REST / SOAP / HTTP testing (using the built-in HttpEasy framework, or plug in your own)
* Database testing

It encourages collaboration and communication between business people, testers and developers. The rich output presents multiple views of the specification with embedded storyboards and logs containing screenshots, HTML page source, message interactions and text.

The framework is easy to configure, for example supported browsers are simple to install locally using the Managed WebDriver, or in the cloud using SauceLabs or BrowserStack. It supports standard patterns for Page Objects and Components to provide the right level of abstraction for web pages.

It is built in Java using [Concordion](http://concordion.org) and incorporates numerous extensions.

## Contributing

All contributions are welcome, we do ask however that you follow our coding standards.  This is rather easy as we have checkstyle and code formatter configuration files ready to use.

For eclipse:

1. Install findbugs plugin

1. Install checkstyle plugin

1. Eclipse > Preferences > Checkstyle > New...

    Type: External configuration file
    Name: Cubano
    Location: <workspace>/cubano/config/checkstyle/checkstyle.xml

1. Eclipse > Preferences > Java > Code Style > Formatter > Import...

	File: <workspace>/cubano/config/formatter/formatter.xml

1. Eclipse > Preferences > Java > Editor > Save Actions

	Perform the selected actions on save: Checked
	Format source code: Format edited lines
	Organise imports: Checked

1. Eclipse > Project > Properties > Checkstyle

	Checkstyle active for this project: Checked
	Use the following check configuration for all files: Cubano - (Global)

## Releasing

To create a release, you will need to run `gradlew clean test javadoc build sourcesJar javadocJar publishDocs release bintrayUpload`. This depends on having appropriate permissions to publish to the Concordion organisation. The build takes over 15 minutes since it also releases to Maven Central. It will then take about another 15 minutes for the artifacts to appear in [Maven Central](https://repo.maven.apache.org/maven2/org/concordion/). 

To build a local release and publish to your local Maven repository, you can run `gradlew clean publishToMavenLocal`.

## Valued Partners

Thanks to [Te Manatū Whakahiato Ora, the Ministry of Social Development, NZ](http://www.msd.govt.nz/) for contributing this framework to the open source community.

[![https://catalogue.data.govt.nz/uploads/group/2017-06-09-002021.456412image.jpg]](http://www.msd.govt.nz/) 

---

Thanks to [Structure 101](http://structure101.com/) for their software architecture visualisation tool - helping us keep the architecture in check. 

[![Structure 101](http://structure101.com/static-content/images/s101_170.png)](http://structure101.com/)
