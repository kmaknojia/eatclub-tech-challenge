package au.com.eatclub.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link TimeDeserializer} which is responsible for converting
 * time strings in 12-hour format (e.g., "2:30pm") to {@link LocalTime} objects.
 */
class TimeDeserializerTest {

    private ObjectMapper objectMapper;
    private ObjectReader timeReader;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalTime.class, new TimeDeserializer());
        objectMapper.registerModule(module);
        timeReader = objectMapper.readerFor(LocalTime.class);
    }

    @ParameterizedTest
    @MethodSource("validTimeFormats")
    void shouldDeserializeValidTimeFormats(String timeString, LocalTime expected) throws JsonProcessingException {
        String json = String.format("\"%s\"", timeString);
        LocalTime result = timeReader.readValue(json);
        assertEquals(expected, result, String.format("Failed to parse time: %s", timeString));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12:00",
            "12pm",
            "12:00:00m",
            "12:00xpm",
            "12:00 AM",
    })
    void shouldHandleInvalidTimeFormats(String invalidTime) {
        String json = String.format("\"%s\"", invalidTime);
        assertThrows(IllegalArgumentException.class,
            () -> timeReader.readValue(json),
            String.format("Should have failed to parse invalid time: %s", invalidTime)
        );
    }

    @Test
    void shouldHandleNullValue() throws JsonProcessingException {
        String json = "null";
        LocalTime result = timeReader.readValue(json);
        assertNull(result, "Should return null for null JSON value");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " ", "\t", "\n"})
    void shouldHandleEmptyOrBlankStrings(String value) throws JsonProcessingException {
        String json = value == null ? "null" : String.format("\"%s\"", value);
        if (value == null) {
            LocalTime result = timeReader.readValue(json);
            assertNull(result, "Should return null for empty/blank string: " + value);
        } else {
            assertThrows(IllegalArgumentException.class,
                () -> timeReader.readValue(json),
                "Should have failed for invalid time format: " + value
            );
        }
    }

    private static Stream<Arguments> validTimeFormats() {
        return Stream.of(
                Arguments.of("12:00am", LocalTime.of(0, 0)),
                Arguments.of("12:00pm", LocalTime.of(12, 0)),
                Arguments.of("1:23am", LocalTime.of(1, 23)),
                Arguments.of("11:59pm", LocalTime.of(23, 59)),
                Arguments.of("9:05am", LocalTime.of(9, 5)),
                Arguments.of("04:30pm", LocalTime.of(16, 30)),
                Arguments.of("12:00AM", LocalTime.of(0, 0)),
                Arguments.of("12:00PM", LocalTime.of(12, 0))
        );
    }
}