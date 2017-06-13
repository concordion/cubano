package org.concordion.cubano.driver.http;

//TODO Can this be replaced by one of these?

//http://stackoverflow.com/questions/2793150/using-java-net-urlconnection-to-fire-and-handle-http-requests
// * https://github.com/kevinsawicki/http-request
// * http://http.jcabi.com/

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.MediaType;

/**
 * Fluent wrapper around {@link HttpURLConnection} with full support for HTTP messages such as GET, POST, HEAD, etc
 * and supports the REST and SOAP protocols and parsing JSON and XML responses.
 * <p>
 * <p>
 * This is been designed with as a fluent REST API similar to RestEasy and
 * RestAssurred with the only real difference being that it has great proxy
 * support.
 * </p>
 * <p>
 * <p>
 * There are two starting points for creating a rest request:
 * <p>
 * 1. {@code HttpEasy.withDefaults()} - allows you to set some settings that apply to
 * all requests such as configuring a proxy
 * 1. {@code HttpEasy.request()} - performs the actual call, these HTTP methods are implemented: GET, HEAD, POST, PUT, DELETE
 * </p>
 * <p>
 * <p>
 * Note: if your url can contain weird characters you will want to encode it,
 * something like this: myUrl = URLEncoder.encode(myUrl, "UTF-8");
 * </p>
 * <p>
 * <p>
 * <b>Example</b>
 * </p>
 * <p>
 * <p>
 * <pre>
 * HttpEasyReader r = HttpEasy.request()
 *                          .baseURI(someUrl)
 *                          .path(viewPath + {@literal "?startkey=\"{startkey}\"&endkey=\"{endkey}\"})
 *                          .urlParameters(startKey[0], endKey[0])
 *                          .get();
 *
 * String id = r.jsonPath("rows[0].doc._id").getAsString();
 * String rev = r.jsonPath("rows[0].doc._rev").getAsString();
 * </pre>
 * </p>
 * <p>
 * <p>
 * <b>Error Handling</b>
 * </p>
 * <p>
 * <p>
 * An IOException is thrown whenever a call returns a response code that is not part of the SUCCESS
 * family (ie 200-299).
 * </p>
 * <p>
 * <p>
 * In order to prevent an exception being thrown for an expected response use
 * one of the following methods:
 * <p>
 * * request().doNotFailOn(Integer... reponseCodes)
 * * request().doNotFailOn(Family... responseFamily)
 * </p>
 * <p>
 * <p>
 * <b>Authentication</b>
 * </p>
 * <p>
 * <p>
 * Supports two formats
 * <p>
 * * http://username:password@where.ever
 * * request().authorization(username, password)
 * </p>
 * <p>
 * <p>
 * <b>Host and Certificate Verification</b>
 * </p>
 * <p>
 * <p>
 * There is no fine grained control, its more of an all or nothing approach:
 * </p>
 * <p>
 * <p>
 * <pre>
 * HttpEasy.withDefaults()
 *      .allowAllHosts()
 *      .trustAllCertificates();
 * </pre>
 * </p>
 * <p>
 * <p>
 * <b>Proxy</b>
 * </p>
 * <p>
 * <p>
 * Only basic authentication is supported, although I believe the domain can be added by included "domain/"
 * in front of the username (not tested)
 * </p>
 * <p>
 * <p>
 * <p>
 * <pre>
 * HttpEasy.withDefaults()
 *     .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(user, password))))
 *     .proxyAuth(userName, password)
 *     .bypassProxyForLocalAddresses(true);
 * </pre>
 * </p>
 * <p>
 * <p>
 * <b>Redirects</b>
 * </p>
 * <p>
 * <p>
 * Redirects are NOT automatically followed - at least for REST base calls - even though the documentation
 * for HttpURLConnection says that it should...
 * </p>
 * <p>
 * <p>
 * <pre>
 * HttpEasyReader response = HttpEasy.request()
 *     .doNotFailOn(Family.REDIRECTION)
 *     .path(url)
 *     .head();
 *
 * if (response.getResponseCodeFamily() == Family.REDIRECTION) {
 *     url = response.getHeaderField("Location");
 *     ...
 * }
 * </pre>
 * </p>
 * <p>
 * <p>
 * <b>Logging</b>
 * </p>
 * <p>
 * <p>
 * Logging of requests and responses can be enabled by {@link #logRequestDetails} or, if using Eclipse, the TCP/IP Monitor utility.
 * </p>
 */
public class HttpEasy {
    static final Logger LOGGER = LoggerFactory.getLogger(HttpEasy.class);

    // These only apply per request - but are visible to package
    List<Integer> ignoreResponseCodes = new ArrayList<Integer>();
    List<Family> ignoreResponseFamily = new ArrayList<Family>();

    // These only apply per request
    private String authString = null;
    private String baseURI = "";
    private String path = "";
    private StringBuilder query = new StringBuilder();
    private String startToken = "{";
    private String endToken = "}";
    private Object[] urlParams = new Object[0];
    private DataContentType dataContentType = DataContentType.AUTO_SELECT;
    private Object rawData = null;
    private String rawFileName = null;
    private MediaType rawDataMediaType = null;
    private Map<String, Object> headers = new LinkedHashMap<String, Object>();
    private List<Field> fields = new ArrayList<Field>();
    private LogWriter logWriter = null;
    private boolean logRequestDetails;
    private Integer timeout = null;

    boolean isLogRequestDetails() {
        return logRequestDetails;
    }

    /**
     * @return Default settings object
     */
    public static HttpEasyDefaults withDefaults() {
        return new HttpEasyDefaults();
    }

    /**
     * @return Request object
     */
    public static HttpEasy request() {
        return new HttpEasy();
    }

    /**
     * Add a header to request.
     *
     * @param name  Header name
     * @param value Header value
     * @return A self reference
     */
    public HttpEasy header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    /**
     * Add an authorization header to request.
     *
     * @param username username
     * @param password password
     * @return A self reference
     */
    public HttpEasy authorization(String username, String password) {
        authString = username + ":" + password;
        return this;
    }

    /**
     * Sets a specified timeout value, in milliseconds, to be used when opening a communications link to the resource referenced by this URLConnection,
     * and when reading from Input stream when a connection is established .
     * <p>
     * If the timeout expires a java.net.SocketTimeoutException is raised.
     * <p>
     * A timeout of zero is interpreted as an infinite timeout.
     *
     * @param milliseconds Timeout value in milliseconds
     * @return A self reference
     */
    public HttpEasy setTimeout(int milliseconds) {
        this.timeout = milliseconds;
        return this;
    }

    /**
     * Set the host and port of the URL for the end-point.  baseURI, path and query are helpers only and any of these can take full URL.
     *
     * @param uri The host and port of the URL
     * @return A self reference
     */
    public HttpEasy baseURI(String uri) {
        this.baseURI = uri;
        return this;
    }

    /**
     * Set the path part of the URL for the end-point.  baseURI, path and query are helpers only and any of these can take full URL.
     *
     * @param path The host and port of the URL
     * @return A self reference
     */
    public HttpEasy path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Set the query part of the URL for the end-point.  baseURI, path and query are helpers only and any of these can take full URL.
     *
     * @param query The host and port of the URL
     * @return A self reference
     */
    public HttpEasy query(String query) {
        this.query = new StringBuilder(query);
        return this;
    }

    /**
     * Appends parameter to query portion of url.
     * <p>
     * <ul>
     * <li>If value is null then parameter will not be added</li>
     * <li>If value is empty then parameter will be added as 'name='</li>
     * </ul>
     *
     * @param name  Parameter name
     * @param value Parameter value
     * @return A self reference
     */
    public HttpEasy queryParam(String name, Object value) {
        if (name == null || name.isEmpty()) {
            return this;
        }

        if (value == null) {
            return this;
        }

        if (this.query.length() > 0) {
            this.query.append("&");
        }

        this.query.append(name).append("=").append(value.toString());

        return this;
    }

    /**
     * If called will cause the request and response details to be logged.
     * <p>
     * <p>
     * An alternative to this if using Eclipse is to use the TCP/IP monitor
     * </p>
     *
     * @return A self reference
     */
    public HttpEasy logRequestDetails() {
        this.logRequestDetails = true;
        return this;
    }

    /**
     * Override the default parameter start and end tokens.  By default any part of the url containing {...} is treated as a parameter and
     * replaced by the values passed in by {@link #urlParameters(Object...)}.
     *
     * @param startToken Start token
     * @param endToken   End Token
     * @return A self reference
     */
    public HttpEasy parameterTokens(String startToken, String endToken) {
        this.startToken = startToken;
        this.endToken = endToken;

        return this;

    }

    /**
     * Set the parameter values for the parameters in the URL.
     *
     * @param pathParams A list of parameters to fill in any parameters required by the URL.  These are replaced in the order they are found in the URL.
     * @return A self reference
     */
    public HttpEasy urlParameters(Object... pathParams) {
        this.urlParams = pathParams;
        return this;
    }

    /**
     * Sets the content type request property to "multipart/form-data"
     * Any data that is required must be added via field(name, value, mediaType) method.
     * Files are supported but any other objects must be easily converted to a string value.
     *
     * @return A self reference
     */
    public HttpEasy dataForm() {
        if (this.dataContentType != DataContentType.AUTO_SELECT) {
            throw new InvalidParameterException("Content type cannot be changed once set");
        }

        this.dataContentType = DataContentType.FORM_DATA;
        return this;
    }

    /**
     * Sets the content type request property to "application/x-www-form-urlencoded"
     * Any data that is required must be added via field(name, value) method
     * and the value must be easily converted to a string value.
     *
     * @return A self reference
     */
    public HttpEasy urlEncodedForm() {
        if (this.dataContentType != DataContentType.AUTO_SELECT) {
            throw new InvalidParameterException("Content type cannot be changed once set");
        }

        this.dataContentType = DataContentType.X_WWW_FORM_URLENCODED;
        return this;
    }

    /**
     * Add a simple text field, if urlEncodedForm() has been used will try to guess the content type.
     * <p>
     * <p>
     * See {@link #field(String, Object, MediaType, String)}
     * </p>
     *
     * @param name  Field name
     * @param value Field value - this should not be URL Encoded!
     * @return A self reference
     */
    public HttpEasy field(String name, Object value) {
        return field(name, value, null);
    }

    /**
     * Add a simple text field, if urlEncodedForm() has been used will try to guess the content type.
     * <p>
     * <p>
     * See {@link #field(String, Object, MediaType, String)}
     * </p>
     *
     * @param name  Field name
     * @param value Field value - this should not be URL Encoded!
     * @param type  Field's media type
     * @return A self reference
     */
    public HttpEasy field(String name, Object value, MediaType type) {
        return field(name, value, type, null);
    }

    /**
     * Add a simple text field, if urlEncodedForm() has been used will try to guess the content type.
     * <p>
     * <p>
     * See {@link #field(String, Object, MediaType, String)}
     * </p>
     *
     * @param name  Field name
     * @param value File
     * @param type  File's media type
     * @return A self reference
     */
    public HttpEasy field(String name, File value, MediaType type) {
        return field(name, value, type, null);
    }

    /**
     * Add a text field or attach file. If value is not a file then it must be easily converted to a string.
     * <p>
     * <p>
     * See {@link #field(String, Object, MediaType, String)}
     * </p>
     *
     * @param name     Field's name
     * @param value    Field's value - File, Inputstream, or object easily converted to a string
     * @param type     Field's media type
     * @param fileName Field's file name - only required if field is an InputStream as this is assumed to be an attachment
     * @return A self reference
     */
    public HttpEasy field(String name, InputStream value, MediaType type, String fileName) {
        return field(name, (Object) value, type, fileName);
    }

    /**
     * Add a text field or attach file.
     * <ul>
     * <li>If value is not a file then it must be easily converted to a string</li>
     * <li>If value is an InputStream it is assumed to be a file attachment and the file name must be provided</li>
     * </ul>
     * <p>
     * <p>
     * If urlEncodedForm() or dataForm() have not been called then the form type will be auto selected:
     * if field containing a file or input stream has been added then content type will be set to "multipart/form-data"
     * else it will be set to "application/x-www-form-urlencoded"
     * </p>
     *
     * @param name     Field's name
     * @param value    Field's value, if File or Inputstream will upload content as "multipart/form-data"
     * @param type     Field's media type
     * @param fileName name of
     * @return A self reference
     */
    public HttpEasy field(String name, Object value, MediaType type, String fileName) {
        if (rawData != null) {
            throw new InvalidParameterException("Data cannot be used at the same time as fields");
        }

        if (value instanceof InputStream && (fileName == null || fileName.isEmpty())) {
            throw new InvalidParameterException("InputStream must provide filename");
        }

        fields.add(new Field(name, value, type, fileName));
        return this;
    }

    /**
     * Sets the content type request property to the value of the supplied media type.
     * <p>
     * <p>
     * This can only be called once as passing raw data can only support one text block or binary file.
     * With this in mind files are supported but any other objects must be easily converted to a string value.
     * </p>
     *
     * @param data      File, or object easily converted to a string
     * @param mediaType Media type of the data
     * @return A self reference
     */
    public HttpEasy data(Object data, MediaType mediaType) {
        return data(data, mediaType, null);
    }

    /**
     * Sets the content type request property to the value of the supplied media type.
     * <p>
     * <p>
     * This can only be called once as passing raw data can only support one text block or binary file.
     * With this in mind files are supported but any other objects must be easily converted to a string value.
     * </p>
     *
     * @param data      File, InputStream, or object easily converted to a string
     * @param mediaType Media type of the data
     * @param fileName  name of file
     * @return A self reference
     */
    public HttpEasy data(Object data, MediaType mediaType, String fileName) {
        if (rawData != null) {
            throw new InvalidParameterException("Only a single data value can be added");
        }

        if (data instanceof InputStream && (fileName == null || fileName.isEmpty())) {
            throw new InvalidParameterException("InputStream must provide filename");
        }

        if (this.dataContentType != DataContentType.AUTO_SELECT) {
            throw new InvalidParameterException("Content type cannot be changed once set");
        }

        dataContentType = DataContentType.RAW;
        rawDataMediaType = mediaType;
        rawData = data;
        rawFileName = fileName;

        return this;
    }

    /**
     * Add a list of response codes to ignore that would otherwise case a exception to be thrown.
     * Example: doNotFailOn(HttpURLConnection.HTTP_CONFLICT)
     *
     * @param reponseCodes A list of failing response codes
     * @return A self reference
     */
    public HttpEasy doNotFailOn(Integer... reponseCodes) {
        this.ignoreResponseCodes.addAll(Arrays.asList(reponseCodes));
        return this;
    }

    /**
     * Add a list of response family codes to ignore that would otherwise case a exception to be thrown.
     * Example: doNotFailOn(Family.REDIRECTION)
     *
     * @param responseFamily A list of failing response family codes
     * @return A self reference
     */
    public HttpEasy doNotFailOn(Family... responseFamily) {
        this.ignoreResponseFamily.addAll(Arrays.asList(responseFamily));
        return this;
    }

    /**
     * Set the logger to write to.
     *
     * @param logWriter Log writer implementation
     * @return A self reference
     */
    public HttpEasy withLogWriter(LogWriter logWriter) {
        this.logWriter = logWriter;
        return this;
    }

    /**
     * Performs an HTTP GET.
     *
     * @return The request response wrapped by {@link HttpEasyReader}
     * @throws HttpResponseException if request failed
     * @throws IOException           for connection errors
     */
    public HttpEasyReader get() throws HttpResponseException, IOException {
        return new HttpEasyReader(getConnectionMethod("GET"), this);
    }

    /**
     * Performs an HTTP HEAD.
     *
     * @return The request response wrapped by {@link HttpEasyReader}
     * @throws HttpResponseException if request failed
     * @throws IOException           for connection errors
     */
    public HttpEasyReader head() throws HttpResponseException, IOException {
        return new HttpEasyReader(getConnectionMethod("HEAD"), this);
    }

    /**
     * Performs an HTTP POST.
     *
     * @return The request response wrapped by {@link HttpEasyReader}
     * @throws HttpResponseException if request failed
     * @throws IOException           for connection errors
     */
    public HttpEasyReader post() throws HttpResponseException, IOException {
        return new HttpEasyReader(getConnectionMethod("POST"), this);
    }

    /**
     * Performs an HTTP PUT.
     *
     * @return The request response wrapped by {@link HttpEasyReader}
     * @throws HttpResponseException if request failed
     * @throws IOException           for connection errors
     */

    public HttpEasyReader put() throws HttpResponseException, IOException {
        return new HttpEasyReader(getConnectionMethod("PUT"), this);
    }

    /**
     * Performs an HTTP DELETE.
     *
     * @return The request response wrapped by {@link HttpEasyReader}
     * @throws HttpResponseException if request failed
     * @throws IOException           for connection errors
     */
    public HttpEasyReader delete() throws HttpResponseException, IOException {
        return new HttpEasyReader(getConnectionMethod("DELETE"), this);
    }

    private HttpURLConnection getConnectionMethod(String requestMethod) throws IOException {
        int fifteenSeconds = 15 * 1000;
        DataWriter dataWriter = null;
        URL url = getURL();
        HttpURLConnection connection = getConnection(url);

        setHeaders(connection);

        connection.setRequestMethod(requestMethod);

        if (timeout != null) {
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
        } else {
            connection.setConnectTimeout(fifteenSeconds);
        }

        connection.setInstanceFollowRedirects(false);

        if (requestMethod.equals("POST") || requestMethod.equals("PUT")) {
            dataWriter = getDataWriter(url, connection);

            connection.setDoOutput(true);
        } else {
            if (fields.size() > 0) {
                throw new IllegalStateException("Fields have been specified but the method " + requestMethod + " will not use them, try POST or PUT instead.");
            }
        }

        String authUser = "";

        if (authString != null && !authString.isEmpty()) {
            authUser = authString.substring(0, authString.indexOf(":"));
            authUser = " as user '" + authUser + "'";
        }

        log("Sending " + requestMethod + authUser + " to " + url.toString());
        //LOGGER.debug("Sending {}{} to {}", requestMethod, authUser, url.toString());

        if (logRequestDetails) {
            StringBuilder sb = new StringBuilder();

            for (Entry<String, List<String>> header : connection.getRequestProperties().entrySet()) {
                for (String value : header.getValue()) {
                    sb.append("\t").append(header.getKey()).append(": ").append(value).append(System.lineSeparator());
                }
            }

            log("With Request Headers:" + System.lineSeparator() + sb.toString());
            //LOGGER.trace("With Request Headers:{}{}", System.lineSeparator(), sb);
        }

        connection.connect();

        if (dataWriter != null) {
            dataWriter.write(logRequestDetails ? LOGGER : null);
        }

        return connection;
    }

    private void log(String message) {
        if (logWriter != null) {
            logWriter.info(message);
        } else if (HttpEasyDefaults.getDefaultLogWriter() != null) {
            HttpEasyDefaults.getDefaultLogWriter().info(message);
        } else {
            LOGGER.trace(message);
        }
    }

    private DataWriter getDataWriter(URL url, HttpURLConnection connection) throws UnsupportedEncodingException {
        DataWriter dataWriter = null;

        if (dataContentType == DataContentType.AUTO_SELECT) {
            if (!fields.isEmpty()) {
                if (fieldsHasFile()) {
                    dataContentType = DataContentType.FORM_DATA;
                } else {
                    dataContentType = DataContentType.X_WWW_FORM_URLENCODED;
                }
            }
        }

        switch (dataContentType) {
            case RAW:
                dataWriter = new RawDataWriter(connection, rawData, rawDataMediaType, rawFileName);
                break;

            case FORM_DATA:
                dataWriter = new FormDataWriter(connection, url.getQuery(), fields);
                break;

            case X_WWW_FORM_URLENCODED:
                dataWriter = new FormUrlEncodedDataWriter(connection, url.getQuery(), fields);
                break;

            case AUTO_SELECT:
                break;

            default:
                throw new InvalidParameterException(dataContentType.toString() + " is unknown");
        }

        return dataWriter;
    }

    private boolean fieldsHasFile() {
        for (Field field : fields) {
            if (field.value instanceof File) {
                return true;
            }

            if (field.value instanceof InputStream) {
                return true;
            }
        }

        return false;
    }

    private HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection connection;
        Proxy useProxy = HttpEasyDefaults.getProxy();

        if (HttpEasyDefaults.isBypassProxyForLocalAddresses() && isLocalAddress(url)) {
            useProxy = Proxy.NO_PROXY;
        }

        if (url.getProtocol().equals("https")) {
            connection = (HttpsURLConnection) url.openConnection(useProxy);
        } else {
            connection = (HttpURLConnection) url.openConnection(useProxy);
        }

        return connection;
    }

    private boolean isLocalAddress(URL url) {
        return "localhost, 127.0.0.1".contains(url.getHost());
    }

    private URL getURL() throws MalformedURLException {
        String spec = "";

        if (!containsProtol(path) && !containsProtol(query.toString())) {
            spec = (baseURI == null || baseURI.isEmpty()) ? HttpEasyDefaults.getBaseURI() : baseURI;
        }

        spec = appendSegmentToUrl(spec, path, "/");
        spec = appendSegmentToUrl(spec, query.toString(), "?");
        spec = replaceParameters(spec);

        URL url = new URL(spec);

        if (url.getUserInfo() != null) {
            authString = url.getUserInfo();
            spec = url.toExternalForm().replace(url.getUserInfo() + "@", "");
            url = new URL(spec);
        }

        return url;
    }

    private boolean containsProtol(String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        return url.contains("//");
    }

    private String appendSegmentToUrl(String url, String segment, String join) {
        if (url == null || url.isEmpty()) {
            return segment;
        }

        if (segment == null || segment.isEmpty()) {
            return url;
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        if (!segment.startsWith(join)) {
            segment = join + segment;
        }

        return url + segment;
    }

    private String replaceParameters(String url) {
        int index = 0;
        int param = 0;
        String currentParameter = "";

        // Check all occurrences
        while ((index = url.indexOf(startToken, index)) > 0) {
            if (param < urlParams.length) {
                currentParameter = String.valueOf(urlParams[param]);
                param++;
            }

            if (currentParameter.contains(" ")) {
                throw new IllegalArgumentException("URL Parameter [" + param + "] cannot contain a space");
            }

            url = url.substring(0, index) + currentParameter + url.substring(url.indexOf(endToken) + 1);
        }

        return url;
    }

    private void setHeaders(HttpURLConnection connection) throws UnsupportedEncodingException {
        setProxyAuthorization(connection);
        setAuthorization(connection);

        for (Map.Entry<String, Object> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), String.valueOf(header.getValue()));
        }
    }

    private void setAuthorization(HttpURLConnection connection) {
        if (authString == null || authString.isEmpty()) {
            return;
        }

        // For prior to Java 1.8
        // import org.apache.commons.codec.binary.Base64;
        // connection.setRequestProperty("Authorization", "Basic " + Base64.encodeBase64String(authString.getBytes(StandardCharsets.UTF_8)));
        // is in commons-codec-1.10.jar
        connection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString(authString.getBytes(StandardCharsets.UTF_8)));
    }

    private void setProxyAuthorization(HttpURLConnection connection) {
        if (HttpEasyDefaults.getProxyUser() == null || HttpEasyDefaults.getProxyUser().isEmpty()) {
            return;
        }

        if (HttpEasyDefaults.getProxyPassword() == null || HttpEasyDefaults.getProxyPassword().isEmpty()) {
            return;
        }

        String usernameAndPassword = HttpEasyDefaults.getProxyUser() + ":" + HttpEasyDefaults.getProxyPassword();
        // String proxyAuthString = "Basic " + Base64.encodeBase64String(usernameAndPassword.getBytes(StandardCharsets.UTF_8));
        String proxyAuthString = "Basic " + Base64.getEncoder().encodeToString(usernameAndPassword.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Proxy-Authorization", proxyAuthString);
    }

    /**
     * Supported form types.
     */
    private enum DataContentType {
        AUTO_SELECT, RAW, X_WWW_FORM_URLENCODED, FORM_DATA;
    }
}
