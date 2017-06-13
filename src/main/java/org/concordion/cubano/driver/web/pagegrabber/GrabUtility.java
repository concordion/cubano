package org.concordion.cubano.driver.web.pagegrabber;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.concordion.cubano.driver.http.HttpEasy;
import org.concordion.cubano.driver.web.PageHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchFrameException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all the utility methods used by GrabWebPage class.
 *
 * @author Pramod Khare
 * @author Andrew Sumner
 */
class GrabUtility {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrabUtility.class);

    private WebDriver driver;
    private List<WebElement> frameTree = new ArrayList<WebElement>();
    private String baseUrl = "";
    private URL fromHTMLPageUrl;
    private Document responseHTMLDoc = null;
    private String outputFolder;
    private final boolean useJavascriptToDownloadFiles = false;

    /**
     * Down-loaded link details.
     */
    private static class GrabbedFile {
        String originalLink;
        String filePath;
    }

    private List<GrabbedFile> grabbedFiles = new ArrayList<GrabbedFile>();

    /**
     * Constructor.
     *
     * @param driver WebDriver
     */
    public GrabUtility(WebDriver driver) {
        this.driver = driver;
    }


    /**
     * Saves current page and any referenced files belonging to the same domain.
     *
     * @param outputFolder Folder to save file to
     * @param fileName     Name for saved page
     * @throws IOException
     */
    public void savePageTo(final String outputFolder, final String fileName) throws IOException {
        this.outputFolder = outputFolder;
        this.fromHTMLPageUrl = new URL(driver.getCurrentUrl());

        selectMainDocument();

        savePage(fileName);
    }

    /**
     * Save frame to file.
     *
     * @param outputFolder Folder to write frame to
     * @param frameName    Name or Id of frame
     * @param frameTree    List of parent frames for the frame to be extracted
     * @param frame        Frame extracting
     * @param frameUrl     Url of the frame
     * @return Name of file saved to
     * @throws IOException
     */
    public String saveFrameTo(final String outputFolder, final String frameName,
                              final List<WebElement> frameTree, final WebElement frame, final String frameUrl) throws IOException {
        this.outputFolder = outputFolder;
        this.fromHTMLPageUrl = new URL(frameUrl);
        this.frameTree.addAll(frameTree);
        this.frameTree.add(frame);

        String fileName = "Frame_" + frameName + ".html";

        try {
            selectMainDocument();
        } catch (NoSuchFrameException e) {
            LOGGER.warn(e.getMessage());
            return "";
        }

        savePage(fileName);

        return fileName;
    }

    private void savePage(String fileName) throws IOException {
        getPageSource();
        getBaseURL();
        getLinks();
        getScripts();
        getImages();

        getIFrames();

        writePage(fileName);
    }

    private void selectMainDocument() {
        PageHelper.switchToMainDocument(driver);

        for (WebElement webElement : frameTree) {
            driver.switchTo().frame(webElement);
        }
    }

    private void getPageSource() {
        String htmlContent = driver.getPageSource();


//      String htmlContent = (String)((JavascriptExecutor)driver).executeScript(return document.documentElement.outerHTML;");


        if (!htmlContent.trim().equals("")) {
            responseHTMLDoc = Jsoup.parse(htmlContent);
        }
    }

    private void getBaseURL() {
        // Check for base tag
        Elements bases = responseHTMLDoc.select("base");

        // Base Tag
        for (Element base : bases) {
            baseUrl = base.attr("href");

            base.remove();
        }
    }

    private void getLinks() throws IOException {
        Elements links = responseHTMLDoc.select("link[href]");

        for (Element link : links) {
            String url = link.attr("href");

            if (download(url)) {
                link.attr("href", getGrabbedFile(url));
            }
        }
    }

    private void getScripts() throws IOException {
        // All external scripts
        Elements scripts = responseHTMLDoc.select("script[src]");
        for (Element script : scripts) {
            String url = script.attr("src");

            if (download(url)) {
                script.attr("src", getGrabbedFile(url));
            }
        }
    }

    private void getImages() throws IOException {
        // All images
        Elements images = responseHTMLDoc.select("img[src]");
        for (Element image : images) {
            String url = image.attr("src");

            if (download(url)) {
                image.attr("src", getGrabbedFile(url));
            }
        }
    }

    private void getIFrames() throws IOException {
        Elements iframes = responseHTMLDoc.select("iframe[src]");
        int frameIndex = -1;

        // Update iFrame with current source
        for (Element iframe : iframes) {
            frameIndex++;

            selectMainDocument();

            WebElement frame = null;
            String attribute;

//            try {
//                driver.switchTo().frame(frame);
//            } catch (NoSuchElementException e) {
//                LOGGER.warn(e.getMessage());
//                continue;
//            }

            attribute = iframe.attr("id");
            if (!attribute.isEmpty()) {
                frame = driver.findElement(By.id(attribute));
            }

            if (frame == null) {
                attribute = iframe.attr("name");
                if (!attribute.isEmpty()) {
                    frame = driver.findElement(By.name(attribute));
                }
            }

//            if (frame == null) {
//                attribute = iframe.className();
//                if (!attribute.isEmpty()) {
//                    frame = driver.findElement(By.className(attribute));
//                }
//            }

            if (frame == null) {
                attribute = "Index" + String.valueOf(frameIndex);
                frame = driver.findElements(By.tagName("iframe")).get(frameIndex);
            }

            String link = iframe.attr("src");
            String fullLink = getFullLink(link);

            if (!fullLink.isEmpty()) {
                String file = new GrabUtility(driver).saveFrameTo(outputFolder, attribute, frameTree, frame, fullLink);

                iframe.attr("src", file);
            }
        }
    }

    private boolean download(String link) throws IOException {
        String fullLink = getFullLink(link);

        if (fullLink.isEmpty()) {
            return false;
        }

        if (hasGrabbedFile(link)) {
            return true;
        }

        File file;

        if (useJavascriptToDownloadFiles) {
            file = getWebObjectViaJavascript(fullLink);
        } else {
            file = getWebObject(fullLink);
        }

        if (file == null) {
            return false;
        }

        GrabbedFile newlink = new GrabbedFile();
        newlink.originalLink = link;
        newlink.filePath = file.getName();

        grabbedFiles.add(newlink);

        return true;
    }

    private boolean hasGrabbedFile(String link) {
        return !getGrabbedFile(link).isEmpty();
    }

    private String getGrabbedFile(String link) {
        for (GrabbedFile file : grabbedFiles) {
            if (file.originalLink.equals(link)) {
                return file.filePath;
            }
        }

        return "";
    }

    private String getFullLink(String link) {
        String fullLink = "";

        if (link.contains("//")) {
            try {
                URL url = new URL(link);

                if (isValidlink(url, fromHTMLPageUrl)) {
                    // Full Domain Link
                    fullLink = link;
                } else {
                    //if link from different domain
                    return "";
                }
            } catch (MalformedURLException e) {
                //logger.warn("Invalid url encountered while downloading link {}: {}", link, e.getMessage());
                return "";
            }
        } else if (!baseUrl.isEmpty()) {
            if (baseUrl.contains("//")) {
                // Absolute Link
                fullLink = baseUrl + link;
            } else {
                // Absolute Link
                link = baseUrl + link;
                fullLink = getRootUrlString(fromHTMLPageUrl) + link;
            }
        } else if (link.startsWith("/")) {
            // Absolute Link
            fullLink = getRootUrlString(fromHTMLPageUrl) + link;
        } else {
            // Relative Link (from current directory)
            fullLink = getCurrentFolder(fromHTMLPageUrl) + link;
        }

        return fullLink;
    }


    private String getCurrentFolder(URL url) {
        String port = (url.getPort() == -1) ? "" : (":" + String.valueOf(url.getPort()));
        String path = url.getPath();
        String currentFolderPath = path.substring(0, path.lastIndexOf("/") + 1);
        return url.getProtocol() + "://" + url.getHost() + port + currentFolderPath;
    }

    private String getRootUrlString(URL url) {
        String port = (url.getPort() == -1) ? "" : (":" + String.valueOf(url.getPort()));
        return url.getProtocol() + "://" + url.getHost() + port;
    }

    // links like mailto, .pdf, or any file downloads, are not to be crawled
    private boolean isValidlink(URL link, URL fromHTMLPageUrl) {
        return getRootUrlString(link).equalsIgnoreCase(getRootUrlString(fromHTMLPageUrl));
    }

    private void writePage(String fileName) throws IOException {
        String htmlContent = responseHTMLDoc.outerHtml();

        FileOutputStream fop = null;
        try {
            // clear previous files contents
            fop = new FileOutputStream(new File(outputFolder, fileName));
            fop.write(htmlContent.getBytes(StandardCharsets.UTF_8));
            fop.flush();
        } finally {
            if (fop != null) {
                fop.close();
            }
        }
    }

    private synchronized File getWebObject(String url) throws IOException {
        try {
            return HttpEasy.request().
                    header("Accept-Language", "en-US,en;q=0.8").
                    header("User-Agent", "Java").
                    header("Referer", "google.com").
                    header("Cookie", mimicCookieState(this.driver.manage().getCookies()).toString()).
                    path(url).
                    get().
                    downloadFile(outputFolder);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }

    private String getFileName(String url) throws IOException {
        URL source = new URL(url);
        String fileName = source.getPath();

        if (source.getQuery() != null || fileName == null || fileName.isEmpty()) {
            throw new IOException("Unable to get fileName from url:" + url);
        }

        return new File(fileName).getName();
    }

    // TODO this largely seems to work but when attempt to download dojo.js it never returns a response.
    // Possibly due to large file size: http://stackoverflow.com/questions/3482596/jquery-get-async-big-file-blocks-browser
    private synchronized File getWebObjectViaJavascript(String url) throws IOException {
        try {
            injectJQuery();
            String fileName = getFileName(url);

//            url = url.replace(AppConfig.getBaseUrl(), "");

            StringBuilder sb = new StringBuilder();

            sb.append("var callback = arguments[arguments.length - 1];");
            sb.append("$.ajax({");
            sb.append("    timeout: 3000,");
            sb.append("    type: 'GET',");
            sb.append("    url: '").append(url).append("',");
            //sb.append("    dataType: \"text\",");
            sb.append("    success: function(data) {");
            //sb.append("        alert(data);");
            sb.append("        callback(data);");
            sb.append("    },");
            sb.append("    error: function() {");
            sb.append("        callback(null);");
            sb.append("    }");
            sb.append("});");

            LOGGER.trace("Downloading " + url);
            driver.manage().timeouts().setScriptTimeout(5, TimeUnit.SECONDS);
            Object content = ((JavascriptExecutor) driver).executeAsyncScript(sb.toString());

//            Object object = (Object) ((JavascriptExecutor) driver).executeScript(sb.toString());
//
//            if (content instanceof String) {
//                content += "";
//            }

            return writeFile(fileName, (String) content);

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return null;
        }
    }


    private File writeFile(String fileName, String result) throws IOException, FileNotFoundException {
        if (result == null) {
            return null;
        }

        File folder = new File(outputFolder);
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                throw new IOException("Unable to create the folder " + folder.getPath());
            }
        }

        final int bufferSize = 4096;
        File saveFile = new File(outputFolder, fileName);

        try (
                InputStream inputStream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
                FileOutputStream outputStream = new FileOutputStream(saveFile);
        ) {

            int bytesRead = -1;
            byte[] buffer = new byte[bufferSize];
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return saveFile;
    }


    // driver
    private boolean hasJQuery() {
        return (boolean) ((JavascriptExecutor) driver).executeScript("return !(typeof jQuery === 'undefined');");
    }

    private String getJQueryFile() throws IOException {
//        URL url = GrabUtility.class.getResource("jquery.js");
//        if (url == null) {
//            throw new IllegalArgumentException("File not found: " + "jquery.js");
//        }
//
//        return url;
        InputStream input = GrabUtility.class.getResourceAsStream("jquery.js");
        if (input == null) {
            throw new IllegalArgumentException("File not found: " + "jquery.js");
        }

        return IOUtils.toString(input, StandardCharsets.UTF_8.name());
    }

    private void injectJQuery() throws Exception {
        if (hasJQuery()) {
            return;
        }

        String jquery = getJQueryFile();
//        String jquery2 = "file://C:/Java/workspace/test-automation-declarewages/bin/nz/govt/msd/driver/web/pagegrabber/jquery.js";

        String inject = "var s=window.document.createElement('script');" +
                //"s.src='" + jquery.toString() + "';" +
                "s.innerHTML = arguments[0];" +
                "window.document.head.appendChild(s);";

        ((JavascriptExecutor) driver).executeScript(inject, jquery);
    }

    // helper method
//    private static String readFile(String file) throws IOException {
//        Charset cs = Charset.forName("UTF-8");
//        FileInputStream stream = new FileInputStream(file);
//        try {
//            Reader reader = new BufferedReader(new InputStreamReader(stream, cs));
//            StringBuilder builder = new StringBuilder();
//            char[] buffer = new char[8192];
//            int read;
//            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
//                builder.append(buffer, 0, read);
//            }
//            return builder.toString();
//        }
//        finally {
//            stream.close();
//        }        
//    }

    /**
     * Get the cookie state that WebDriver is using so that the request can use the same session as WebDriver
     * and keep any authentication settings.
     */
    private BasicCookieStore mimicCookieState(Set<Cookie> seleniumCookieSet) {
        BasicCookieStore mimicWebDriverCookieStore = new BasicCookieStore();
        for (Cookie seleniumCookie : seleniumCookieSet) {
            BasicClientCookie duplicateCookie = new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            duplicateCookie.setDomain(seleniumCookie.getDomain());
            duplicateCookie.setSecure(seleniumCookie.isSecure());
            duplicateCookie.setExpiryDate(seleniumCookie.getExpiry());
            duplicateCookie.setPath(seleniumCookie.getPath());
            mimicWebDriverCookieStore.addCookie(duplicateCookie);
        }

        return mimicWebDriverCookieStore;
    }
}