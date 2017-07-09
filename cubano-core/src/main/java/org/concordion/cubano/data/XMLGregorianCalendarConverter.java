package org.concordion.cubano.data;

import java.lang.reflect.Type;

import javax.xml.datatype.XMLGregorianCalendar;

import org.concordion.cubano.utils.ISODateTimeFormat;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serialise and deserialise XMLGregorianCalendar datatype to/from JSON.
 */
public class XMLGregorianCalendarConverter {
    /**
     * Serialise XMLGregorianCalendar datatype to/from JSON.
     */
    public static class Serializer implements JsonSerializer<Object> {

        /**
         * Serializer.
         *
         * @param t                        data
         * @param type                     type
         * @param jsonSerializationContext context
         * @return JsonElement
         */
        public JsonElement serialize(Object t, Type type, JsonSerializationContext jsonSerializationContext) {
            XMLGregorianCalendar xgcal = (XMLGregorianCalendar) t;
            return new JsonPrimitive(xgcal.toXMLFormat());
        }
    }

    /**
     * Deserialise XMLGregorianCalendar datatype to/from JSON.
     */
    public static class Deserializer implements JsonDeserializer<Object> {

        @Override
        public Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            String value = jsonElement.getAsString();

            try {
                if (value == null || value.isEmpty()) {
                    return null;
                }

                return ISODateTimeFormat.toXMLGregorianCalendar(jsonElement.getAsString());
            } catch (Exception e) {
                throw new JsonParseException("Unable to parse XMLGregorianCalendar value '" + value + "'", e);
            }
        }
    }
}
