package org.concordion.cubano.driver.concordion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.concordion.api.Element;
import org.concordion.api.Resource;
import org.concordion.api.extension.ConcordionExtender;
import org.concordion.api.extension.ConcordionExtension;
import org.concordion.api.listener.SpecificationProcessingEvent;
import org.concordion.api.listener.SpecificationProcessingListener;

/**
 * Displays the environment details to the left hand side of the standard Concordion footer.
 * <p>
 * <p>
 * Sample usage:
 * <p>
 * <pre>
 * &#64;Extension
 * private final EnvironmentExtension footer = new EnvironmentExtension()
 *      .withRerunTest("MyMSD-RunSelectedTest")
 *      .withRerunParameter("token", "ALLOW")
 *      .withRerunParameter("TEST_CLASSNAME", this.getClass().getName().replace(ConcordionBase.class.getPackage().getName() + ".", ""))
 *      .withEnvironment(AppConfig.getEnvironment().toUpperCase())
 *      .withURL(AppConfig.getUrl());
 * </pre>
 * </p>
 * <p>
 * Note:
 * <ul>
 * <li>The only required parameter for rerunning tests is "token" which must be configured in the Jenkins job</li>
 * <li>Requires custom css that must be added manually</li>
 * <li>Job will need to pass -DJENKINS_URL=${JENKINS_URL} -DSVN_URL=${SVN_URL} (if using subversion) so that we can pick up those values</li>
 * </ul>
 * </p>
 */
public class EnvironmentExtension implements ConcordionExtension, SpecificationProcessingListener {
    private String jobName = "";
    private Map<String, String> rerunParameters = new LinkedHashMap<>();

    private String environment = "";
    private String url = "";
    private String urlLabel = "";

    @Override
    public void addTo(final ConcordionExtender concordionExtender) {
        concordionExtender.withSpecificationProcessingListener(this);

        String path = "/" + EnvironmentExtension.class.getPackage().getName().replace(".", "/") + "/";
        concordionExtender.withLinkedJavaScript(path + "rerun.js", new Resource("/rerun.js"));

    }

    /**
     * Set the name of the test to append a "rerun" link in the footer of each specification for.
     *
     * @param jobName Name of the Jenkins job used to rerun a test
     * @return A self reference
     */
    public EnvironmentExtension withRerunTest(String jobName) {
        this.jobName = jobName == null ? "" : jobName.replace(" ", "%20");

        return this;
    }

    /**
     * Set the name and value of a Jenkins rerun test job parameter to append to the "rerun" link.
     * {@link #withRerunTest(String)} must also be called.
     *
     * @param name  Jenkins job parameter name
     * @param value If rerun Jenkins job accepts a subversion tag parameter then supply the name
     * @return A self reference
     */
    public EnvironmentExtension withRerunParameter(String name, String value) {
        rerunParameters.put(name, value);

        return this;
    }

    /**
     * Set the name of the environment to display in the footer of each specification.
     *
     * @param environment Envrionment name
     * @return A self reference
     */
    public EnvironmentExtension withEnvironment(String environment) {
        this.environment = environment == null ? "" : environment;

        return this;
    }

    /**
     * Set the URL of the environment to display in the footer of each specification.
     *
     * @param url Environment URL
     * @return A self reference
     */
    public EnvironmentExtension withURL(String url) {
        withURL(url, url);

        return this;
    }

    /**
     * Set the URL of the environment to display in the footer of each specification.
     *
     * @param url   Environment URL
     * @param label Label to show in place of the URL
     * @return A self reference
     */
    public EnvironmentExtension withURL(String url, String label) {
        this.url = url == null ? "" : url;
        this.urlLabel = label == null ? "" : label;

        return this;
    }


    @Override
    public void beforeProcessingSpecification(SpecificationProcessingEvent event) {

    }

    @Override
    public void afterProcessingSpecification(SpecificationProcessingEvent event) {
        Element leftFooter = getLeftFooter(event);

        appendEnviromentToFooter(leftFooter);
        appendJenkinsRerunToFooter(leftFooter);
    }

    private Element getLeftFooter(final SpecificationProcessingEvent event) {
        Element body = event.getRootElement().getFirstChildElement("body");

        if (body == null) {
            return null;
        }

        Element[] divs = body.getChildElements("div");
        for (Element div : divs) {
            if ("footer".equals(div.getAttributeValue("class"))) {
                Element footer = div;
                Element leftFooter = new Element("div");

                leftFooter.addStyleClass("footerLeft");

                footer.prependChild(leftFooter);

                return leftFooter;
            }
        }

        return null;
    }

    private void appendEnviromentToFooter(Element leftFooter) {
        if (environment.isEmpty() && url.isEmpty()) {
            return;
        }

        if (!environment.isEmpty()) {
            leftFooter.appendText(environment);
        }

        if (!url.isEmpty()) {
            leftFooter.appendText(" (");

            Element anchor = new Element("a");
            anchor.addAttribute("href", url);
            anchor.addAttribute("style", "text-decoration: none; color: #89C;");
            anchor.appendText(urlLabel);

            leftFooter.appendChild(anchor);
            leftFooter.appendText(")");
        }
    }

    private void appendJenkinsRerunToFooter(Element leftFooter) {
        String jenkinsUrl = System.getProperty("JENKINS_URL", "");

        if (jenkinsUrl.isEmpty() || jobName.isEmpty()) {
            return;
        }

        StringBuilder jobUrl = new StringBuilder(jenkinsUrl).append("job/").append(jobName);

        StringBuilder rerunUrl = new StringBuilder(jobUrl).append("/buildWithParameters?");

        boolean andRequired = false;

        for (Entry<String, String> parameter : rerunParameters.entrySet()) {
            if (andRequired) {
                rerunUrl.append("&");
            }

            rerunUrl.append(parameter.getKey()).append("=").append(parameter.getValue());
            andRequired = true;
        }

        Element anchor = new Element("a");
        anchor.addAttribute("href", "#");
        anchor.addAttribute("onclick", "runtest('" + jobUrl + "', '" + rerunUrl + "'); return false;");
        anchor.addAttribute("style", "text-decoration: none; color: #89C; font-weight: bold;");
        anchor.appendText("Rerun this test");

        leftFooter.appendChild(new Element("br"));
        leftFooter.appendChild(anchor);
    }

    /**
     * @return Subversion URL to pass when using a Subverion Tag parameter.
     */
    public static String getSubversionUrl() {
        String svnUrl = System.getProperty("SVN_URL", "");

        if (!svnUrl.isEmpty()) {
            String[] repos = {
                    "/trunk", "/tags/", "/branches/"
            };

            for (String repo : repos) {
                int index = svnUrl.indexOf(repo);

                if (index > -1) {
                    svnUrl = svnUrl.substring(index + 1);
                    break;
                }
            }
        }

        return svnUrl;
    }
}
