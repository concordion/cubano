[![Build and Test](https://github.com/concordion/cubano/actions/workflows/ci.yml/badge.svg)](https://github.com/concordion/cubano/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/org.concordion/cubano-concordion.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.concordion%22%20AND%20a%3A%22cubano-concordion%22)
[![Apache License 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# cubano

Cubano is a "ready-made" test framework that provides everything at your fingertips. It's ideal for software delivery teams who want to collaborate around living documentation.

Utilising __Specification by Example__ techniques, the framework implements:

* Web browser automation using Selenium WebDriver
* API automation (REST / SOAP / HTTP)
* Database automation

Cubano supplies [User Documentation](https://concordion.org/cubano/index), and a [Demo Project](https://github.com/concordion/cubano-demo#cubano-demo-project), to help users get started. 

It encourages collaboration and communication between business people, testers and developers. The rich output presents multiple views of the specification with embedded storyboards and logs containing screenshots, HTML page source, message interactions and text.

The framework is easy to configure, for example supported browsers are simple to install locally using the Managed WebDriver, or in the cloud using SauceLabs or BrowserStack. It supports standard patterns for Page Objects and Components to provide the right level of abstraction for web pages.

It is built in Java using [Concordion](https://concordion.org) and incorporates numerous extensions.

## Cubano Demo and Template Projects
Cubano provides a [Demo Project](https://github.com/concordion/cubano-demo#cubano-demo-project), which contains information regarding
* usage
* documentation
* working examples 
* and set up instructions 

Once you are ready to start writing your own tests, pull down the [Cubano Template Project](https://github.com/concordion/cubano-template) and adapt this base to start automating your project.

## Feedback

We love to receive feedback.

Please report any [issues](https://github.com/concordion/cubano/issues) to this Github project.

Please post other feedback, questions and discussion to the [Concordion Google Group](https://groups.google.com/forum/#!forum/concordion) with "[Cubano]" in the message header.


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

The project is released to Maven Central using a few plugins defined in `build.gradle`.

### <a name="Pre-conditions"></a>Pre-conditions[](#Pre-conditions)

*   A GPG client is installed on your command line path. For more information, please refer to [http://www.gnupg.org/](http://www.gnupg.org/).
*   You have created your GPG keys and distributed your public key. For more information, please refer to [Gradle Signing Plugin](https://docs.gradle.org/current/userguide/signing_plugin.html).
*   You have a [Sonatype JIRA account](https://issues.sonatype.org), which has [approval](https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide#SonatypeOSSMavenRepositoryUsageGuide-3.CreateaJIRAticket) for publishing to the Concordion project.

*   You have configured your gradle.properties, typically located in your ~/.gradle directory: 
```
sonatypeUsername=<your-jira-username>
sonatypePassword=<your-jira-password>

signing.keyId=<public GPG key>
signing.password=<private GPG key passphrase>
signing.secretKeyRingFile=<path to secret key ring file containing your private key>
```
where _<your-jira-username>_ and _<your-jira-password>_ are the credentials for your Sonatype JIRA account.

<!--
# Publishing a snapshot

*   Ensure the `gradle.properties` file contains the correct version, and ends with `-SNAPSHOT`.
*   Commit and push all the changes to GitHub.
*   Run `./gradlew -b publish.gradle publishSnapshot`.
*   The snapshot should appear under [https://oss.jfrog.org/artifactory/libs-snapshot/org/concordion](https://oss.jfrog.org/artifactory/libs-snapshot/org/concordion).
-->

### Performing a release

* Check that `gradle.properties` contains the desired version number.
* Commit and push all the changes to GitHub. (The release plugin will fail if you have any changes that aren't committed and pushed.)
* Checkout the master branch, if not already checked out.
* Either run `./gradlew clean test javadoc gitPublishPush publishToSonatype closeSonatypeStagingRepository` and manually release the build from the Nexus staging repository or run `./gradlew clean test javadoc gitPublishPush publishToSonatype closeAndReleaseSonatypeStagingRepository` if you are confident. See the [Gradle Nexus Publish Plugin](https://github.com/gradle-nexus/publish-plugin) for more details.
<!--During this build, Gradle will prompt for the version number of the release, and for the next version number to use. See [here](https://github.com/townsfolk/gradle-release#introduction) for the steps taken by the release plugin.-->
* Create [release notes](https://github.com/concordion/cubano/releases) on Github, selecting the version number of the release as the tag.

To build a local release and publish to your local Maven repository, you can run `gradlew clean publishToMavenLocal`.

## Valued Partners

Thanks to [Te Manatū Whakahiato Ora, the Ministry of Social Development, NZ](http://www.msd.govt.nz/) for contributing this framework to the open source community.

<img src="https://catalogue.data.govt.nz/uploads/group/2017-06-09-002021.456412image.jpg" alt="MSD logo" width="200"/>

---

Thanks also to [Structure 101](http://structure101.com/) for their software architecture visualisation tool - helping us keep the architecture in check. 

[![Structure 101](http://structure101.com/static-content/images/s101_170.png)](http://structure101.com/)
