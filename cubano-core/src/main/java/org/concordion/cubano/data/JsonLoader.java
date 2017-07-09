package org.concordion.cubano.data;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import org.concordion.cubano.driver.http.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * Utility class for loading JSON files.
 *
 * @author Andrew Sumner
 */
public class JsonLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLoader.class);

    private JsonLoader() {
    }

    /**
     * Read data file and deserialise into new class of requested type.
     *
     * @param <T>      Desired class
     * @param jsonFile Source file
     * @param clazz    Desired class
     * @return Desired class populated from requested file
     * @throws IOException
     */
    public static <T> T loadFile(String jsonFile, Class<T> clazz) throws IOException {
        return loadFile(jsonFile, (Type) clazz);
    }

    /**
     * Read data file and deserialise into new class of requested type.
     *
     * @param <T>        Desired class
     * @param jsonFile   Source file
     * @param returnType Desired class
     * @return Desired class populated from requested file
     * @throws IOException
     */
    public static <T> T loadFile(String jsonFile, Type returnType) throws IOException {
        LOGGER.debug("Loading JSON file {}", jsonFile);
        JsonReader json = new JsonReader(FileReader.readFile(jsonFile));

        Gson builder = new GsonBuilder()
                .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarConverter.Deserializer())
                .registerTypeAdapter(XMLGregorianCalendar.class, new XMLGregorianCalendarConverter.Serializer())
                .create();

        T result = json.fromJson(builder, returnType);

        validateAllDataLoaded(json.asJson(), result, "");

        return result;
    }

    private static void validateAllDataLoaded(JsonElement element, Object returnType, String entryName) throws IOException {

        if (element.isJsonNull()) {
            return;
        }

        if (element.isJsonArray()) {
            checkFieldExists(entryName, returnType);

            JsonArray jsonArray = element.getAsJsonArray();
            ArrayList<?> objArray = (ArrayList<?>) returnType;

            if (objArray.size() != jsonArray.size()) {
                throw new IllegalStateException(String.format("Object array size %s does not match JSON array size %s", objArray.size(), jsonArray.size()));
            }

            for (int index = 0; index < objArray.size(); index++) {
                validateAllDataLoaded(jsonArray.get(index), objArray.get(index), entryName);
            }
        }

        if (element.isJsonObject()) {
            checkFieldExists(entryName, returnType);

            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                if (!entry.getValue().isJsonNull() && !entry.getValue().isJsonPrimitive()) {
                    validateAllDataLoaded(entry.getValue(), getField(entry.getKey(), returnType), "");
                } else {
                    validateAllDataLoaded(entry.getValue(), returnType, entry.getKey());
                }
            }
        }

        if (element.isJsonPrimitive()) {
            checkFieldExists(entryName, returnType);
        }
    }

    private static void checkFieldExists(String entryName, Object returnType) throws IOException {
        if (entryName.isEmpty()) {
            return;
        }

        List<Field> fields = getInheritedFields(returnType.getClass());
        boolean exists = fields.stream().filter(f -> f.getName().equals(entryName)).count() == 1;

        if (!exists) {
            throw new IOException("JSON element '" + entryName + "' is not a field in the destination class " + returnType.getClass().getName());
        }
    }

    private static Object getField(String entryName, Object returnType) throws IOException {
        if (entryName.isEmpty()) {
            return null;
        }

        try {
            Field field = getDeclaredField(entryName, returnType.getClass());
            field.setAccessible(true);

            return field.get(returnType);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            throw new IOException(e);
        }
    }

    private static Field getDeclaredField(String entryName, Class<?> clazz) throws NoSuchFieldException, SecurityException {
        NoSuchFieldException lastException = null;
        Class<?> current = clazz;

        while (current != null) {
            try {
                return current.getDeclaredField(entryName);
            } catch (NoSuchFieldException e) {
                lastException = e;
            }

            current = current.getSuperclass();
        }

        throw lastException;
    }

    private static List<Field> getInheritedFields(Class<?> type) {
        List<Field> fields = new ArrayList<Field>();

        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            fields.addAll(Arrays.asList(c.getDeclaredFields()));
        }

        return fields;
    }
}
