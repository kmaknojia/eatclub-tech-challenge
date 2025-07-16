package au.com.eatclub.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Custom JSON deserializer for converting 12-hour format time strings to {@link LocalTime} objects.
 * This deserializer handles time strings in the format "h:mma" (e.g., "2:30PM") and converts
 * them to {@link LocalTime} instances. It properly handles null values and provides meaningful
 * error messages for invalid formats.
 */
@Slf4j
public class TimeDeserializer extends JsonDeserializer<LocalTime> {
    public final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mma");

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws RuntimeException {
        java.util.Date date;
        try {
            String timeStr = p.getValueAsString();
            if (timeStr == null) {
                return null;
            }

            date = simpleDateFormat.parse(timeStr);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
        } catch (Exception e) {
            log.error("error parsing time {}", e.getMessage(), e);
            throw new IllegalArgumentException(e);
        }
    }
}
