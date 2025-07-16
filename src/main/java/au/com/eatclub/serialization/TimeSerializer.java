package au.com.eatclub.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Custom JSON serializer for converting {@link LocalTime} objects to 12-hour format time strings.
 * 
 * This serializer formats time values in the pattern "h:mma" (e.g., "2:30PM") when
 * serializing to JSON. It handles null values by writing null to the JSON output.
 *
 */
public class TimeSerializer extends JsonSerializer<LocalTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("h:mma").withZone(null);

    @Override
    public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null) {
            gen.writeString(value.format(FORMATTER));
        } else {
            gen.writeNull();
        }
    }
}
