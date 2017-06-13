package org.concordion.cubano.driver.http;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * A wrapper around "com.google.gson.Gson" for simplifying the parsing JSON strings.
 *
 * @author Andrew Sumner
 */
public class JsonReader implements ResponseReader {
    private final JsonElement json;
    // Check if has '.' not preceded by '\'
    private static final Pattern CHECK_FOR_DOT = Pattern.compile("(?<!\\\\)\\.");

    /**
     * A json reader.
     *
     * @param json Json string
     */
    public JsonReader(String json) {
        this.json = new JsonParser().parse(json);
    }

    /**
     * A json reader.
     *
     * @param element Json Element
     */
    public JsonReader(JsonElement element) {
        this.json = element;
    }

    /**
     * @return A nicely formatted JSON string
     * @throws IOException If unable to read the response
     */
    @Override
    public String asPrettyString() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    /**
     * Deserialize the returned Json string.
     *
     * @return A {@link JsonElement}
     * @throws IOException if the response string is not valid JSON
     */
    public JsonElement asJson() throws IOException {
        return json;
    }

    /**
     * Search the JSON response for the requested element.
     * Is a helper method for {@link #jsonPath(String)}.
     *
     * @param path A dot separated Json search path
     * @return String if element found otherwise null
     */
    public String getAsString(String path) {
        JsonElement element = jsonPath(path);

        if (element == null || element.isJsonNull()) {
            return null;
        }

        return element.getAsString();
    }

    /**
     * Search the JSON response for the requested element.
     * Is a helper method for {@link #jsonPath(JsonElement, String)}.
     *
     * @param node Json element
     * @param path A dot separated Json search path
     * @return String if element found otherwise null
     */
    public String getAsString(JsonElement node, String path) {
        JsonElement element = jsonPath(node, path);

        if (element == null || element.isJsonNull()) {
            return null;
        }

        return element.getAsString().trim();
    }

    /**
     * Search the JSON response for the requested element.
     * Is a helper method for {@link #jsonPath(String)}.
     *
     * @param path A dot separated Json search path
     * @return JsonArray if element found otherwise empty JsonArray
     */
    public JsonArray getAsJsonArray(String path) {
        JsonElement element = jsonPath(path);

        if (element == null || !element.isJsonArray()) {
            return new JsonArray();
        }

        return element.getAsJsonArray();
    }

    /**
     * Search the JSON response for the requested element.
     * Is a helper method for {@link #jsonPath(JsonElement, String)}.
     *
     * @param node Json element
     * @param path A dot separated Json search path
     * @return JsonArray if element found otherwise empty JsonArray
     */
    public JsonArray getAsJsonArray(JsonElement node, String path) {
        JsonElement element = jsonPath(node, path);

        if (element == null || !element.isJsonArray()) {
            return new JsonArray();
        }

        return element.getAsJsonArray();
    }

    /**
     * Search the JSON response for the requested element.
     * <p>
     * <pre>
     * JsonElement value = reader.jsonPath("anArray[0].aValue").
     * String details = (value == null ? "" : value.getAsString());
     * </pre>
     * <p>
     * <p>
     * If an element name contains a dot then the dot can be escaped with a double backslash, eg:
     * <p>
     * <pre>
     * ele1.ele2part1\\.ele2part2
     * </pre>
     * </p>
     *
     * @param path A dot separated Json search path
     * @return JsonElement or null if not found
     */
    public JsonElement jsonPath(String path) {
        return jsonPath(json, path);
    }

    /**
     * Search a JSON element's children for the requested element.
     * <p>
     * <p>
     * If an element name contains a dot then the dot can be escaped with a double backslash, eg:
     * <p>
     * <pre>
     * ele1.ele2part1\\.ele2part2
     * </pre>
     * </p>
     *
     * @param json Json element
     * @param path A dot separated Json search path
     * @return JsonElement or null if not found
     */
    public JsonElement jsonPath(JsonElement json, String path) {
        if (json == null) {
            return null;
        }

        if (!hasNextJsonPathElement(path)) {
            return json.getAsJsonObject().get(removeEscapeCharacter(path));
        } else {
            JsonElement newJson;
            String next = getNextJsonPathElement(path);

            if (next.endsWith("]")) {
                int pos = next.lastIndexOf('[');
                String index = next.substring(pos + 1, next.length() - 1);
                next = next.substring(0, pos);

                newJson = json.getAsJsonObject().get(next).getAsJsonArray().get(Integer.parseInt(index));
            } else {
                newJson = json.getAsJsonObject().get(next);
            }

            return jsonPath(newJson, getNewJsonPath(path));
        }
    }

    private String removeEscapeCharacter(String path) {
        return path.replace("\\.", ".");
    }

    private boolean hasNextJsonPathElement(String path) {
        Matcher matcher = CHECK_FOR_DOT.matcher(path);
        return matcher.find();
    }

    private String getNextJsonPathElement(String path) {
        Matcher matcher = CHECK_FOR_DOT.matcher(path);

        if (matcher.find()) {
            return removeEscapeCharacter(path.substring(0, matcher.start()));
        } else {
            return path;
        }
    }

    private String getNewJsonPath(String path) {
        Matcher matcher = CHECK_FOR_DOT.matcher(path);

        if (matcher.find()) {
            return path.substring(matcher.end());
        } else {
            return "";
        }
    }

    /**
     * Deserialize the Json into an object of the specified class.
     * <p>
     * <p>
     * Note: use @SerializedName("custom-name") to convert from key name to variable name
     * </p>
     *
     * @param <T>  The type of the desired object
     * @param type Class to populate
     * @return A new class of the supplied type
     * @throws IOException if json is not a valid representation for an object of type classOfT
     */
    public <T> T fromJson(Class<T> type) throws IOException {
        return new Gson().fromJson(json, type);
    }

    /**
     * Deserialize the Json into an object of the specified class using a default builder.
     *
     * @param <T>        The type of the desired object
     * @param returnType Class to populate
     * @return A new class of the supplied type
     * @throws JsonSyntaxException if json is not a valid representation for an object of type classOfT
     */
    public <T> T fromJson(Type returnType) throws JsonSyntaxException {
        return new Gson().fromJson(json, returnType);
    }

    /**
     * Deserialize the Json into an object of the specified class using the supplied builder.
     *
     * @param <T>        The type of the desired object
     * @param builder    Gson builder to use
     * @param returnType Class to populate
     * @return A new class of the supplied type
     * @throws JsonSyntaxException if json is not a valid representation for an object of type classOfT
     */
    public <T> T fromJson(Gson builder, Type returnType) {
        return builder.fromJson(json, returnType);
    }
}
