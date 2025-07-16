package au.com.eatclub.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TimeSerializerTest {

    @Mock
    private JsonGenerator jsonGenerator;

    @Mock
    private SerializerProvider serializerProvider;

    private TimeSerializer timeSerializer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        timeSerializer = new TimeSerializer();
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalTime.class, timeSerializer);
        objectMapper.registerModule(module);
    }

    @Test
    void shouldSerializeTimeTo12HourFormat() throws IOException {
        LocalTime time = LocalTime.of(14, 30); // 2:30 PM
        String expected = "\"2:30pm\"";

        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, time);

        assertEquals(expected, writer.toString());
    }

    @Test
    void shouldHandleMidnight() throws IOException {
        LocalTime midnight = LocalTime.MIDNIGHT;
        String expected = "\"12:00am\"";

        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, midnight);

        assertEquals(expected, writer.toString());
    }

    @Test
    void shouldHandleNoon() throws IOException {
        LocalTime noon = LocalTime.NOON;
        String expected = "\"12:00pm\"";

        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, noon);

        assertEquals(expected, writer.toString());
    }

    @Test
    void shouldHandleSingleDigitHour() throws IOException {
        LocalTime time = LocalTime.of(9, 5);
        String expected = "\"9:05am\"";

        StringWriter writer = new StringWriter();
        objectMapper.writeValue(writer, time);

        assertEquals(expected, writer.toString());
    }

    @Test
    void shouldHandleNullValue() throws IOException {
        LocalTime time = null;

        timeSerializer.serialize(time, jsonGenerator, serializerProvider);

        verify(jsonGenerator, times(1)).writeNull();
        verify(jsonGenerator, never()).writeString(anyString());
    }

    @Test
    void shouldThrowIOExceptionOnWriteError() throws IOException {
        LocalTime time = LocalTime.of(10, 30);
        doThrow(new IOException("Write error")).when(jsonGenerator).writeString(anyString());

        assertThrows(IOException.class, () ->
                timeSerializer.serialize(time, jsonGenerator, serializerProvider)
        );
    }

    @Test
    void shouldHandleAllDayTimes() throws IOException {
        // Test various times throughout the day
        LocalTime[] times = {
                LocalTime.MIDNIGHT,          // 12:00 AM
                LocalTime.of(1, 15),         // 1:15 AM
                LocalTime.of(11, 45),        // 11:45 AM
                LocalTime.NOON,              // 12:00 PM
                LocalTime.of(13, 30),        // 1:30 PM
                LocalTime.of(23, 59)         // 11:59 PM
        };

        String[] expected = {
                "\"12:00am\"",
                "\"1:15am\"",
                "\"11:45am\"",
                "\"12:00pm\"",
                "\"1:30pm\"",
                "\"11:59pm\""
        };

        for (int i = 0; i < times.length; i++) {
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, times[i]);
            assertEquals(expected[i], writer.toString(),
                    String.format("Failed for time: %s", times[i]));
        }
    }

    @Test
    void shouldHandleBoundaryTimes() throws IOException {
        // Test boundary cases
        LocalTime[] times = {
                LocalTime.of(0, 0),     // 12:00 AM (midnight)
                LocalTime.of(11, 59),   // 11:59 AM
                LocalTime.of(12, 0),    // 12:00 PM (noon)
                LocalTime.of(12, 1),    // 12:01 PM
                LocalTime.of(23, 59)    // 11:59 PM
        };

        String[] expected = {
                "\"12:00am\"",
                "\"11:59am\"",
                "\"12:00pm\"",
                "\"12:01pm\"",
                "\"11:59pm\""
        };

        for (int i = 0; i < times.length; i++) {
            StringWriter writer = new StringWriter();
            objectMapper.writeValue(writer, times[i]);
            assertEquals(expected[i], writer.toString(),
                    String.format("Failed for time: %s", times[i]));
        }
    }
}