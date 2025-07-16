// src/test/java/au/com/eatclub/repository/RestaurantRepositoryTest.java
package au.com.eatclub.repository;

import au.com.eatclub.model.Restaurant;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link RestaurantRepository} class.
 * These tests mock the OkHttpClient to simulate various API responses
 * without making actual network calls.
 */
@ExtendWith(MockitoExtension.class) // Enables Mockito annotations for JUnit 5
@MockitoSettings(strictness = Strictness.LENIENT)
class RestaurantRepositoryTest {

    @Mock
    private OkHttpClient mockHttpClient;

    @Mock
    private Call mockCall;

    @Mock
    private Response mockResponse;

    @Mock
    private ResponseBody mockResponseBody;

    @InjectMocks
    private RestaurantRepository restaurantRepository;

    private static final String SUCCESS_JSON_RESPONSE = "{"
            + "\"restaurants\": ["
            + "    {\"objectId\": \"1\", \"name\": \"Restaurant A\"},"
            + "    {\"objectId\": \"2\", \"name\": \"Restaurant B\"}"
            + "]"
            + "}";

    private static final String EMPTY_RESTAURANTS_JSON_RESPONSE = "{"
            + "\"restaurants\": []"
            + "}";

    private static final String MISSING_RESTAURANTS_JSON_RESPONSE = "{"
            + "\"other_data\": \"some_value\""
            + "}";

    private static final String NON_ARRAY_RESTAURANTS_JSON_RESPONSE = "{"
            + "\"restaurants\": \"not_an_array\""
            + "}";

    private static final String MALFORMED_JSON_RESPONSE = "{"
            + "\"restaurants\": ["
            + "    {\"id\": \"1\", \"name\": \"Restaurant A\", \"cuisines\": \"Italian\"" // Missing closing bracket
            + "}";

    @BeforeEach
    void setUp() throws IOException {
        when(mockHttpClient.newCall(any(Request.class))).thenReturn(mockCall);
        when(mockCall.execute()).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(mockResponseBody);
    }

    @Test
    void getRestaurantDataFromApi_success() throws IOException {
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponseBody.string()).thenReturn(SUCCESS_JSON_RESPONSE);

        List<Restaurant> expectedRestaurants = List.of(
                 Restaurant.builder().objectId("1").name("Restaurant A").cuisines(List.of("Italian")).build(),
                 Restaurant.builder().objectId("2").name("Restaurant B").cuisines(List.of("Mexican")).build()
        );

        List<Restaurant> actualRestaurants = restaurantRepository.getRestaurantDataFromApi();

        assertNotNull(actualRestaurants, "The returned list should not be null");
        assertEquals(expectedRestaurants.size(), actualRestaurants.size(), "The number of restaurants should match");
    }

    @Test
    void getRestaurantDataFromApi_emptyRestaurantsArray() throws IOException {
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponseBody.string()).thenReturn(EMPTY_RESTAURANTS_JSON_RESPONSE);

        List<Restaurant> actualRestaurants = restaurantRepository.getRestaurantDataFromApi();

        assertNotNull(actualRestaurants, "The returned list should not be null");
        assertTrue(actualRestaurants.isEmpty(), "The list of restaurants should be empty");
    }

    @Test
    void getRestaurantDataFromApi_unsuccessfulResponse() {
        when(mockResponse.isSuccessful()).thenReturn(false);
        when(mockResponse.code()).thenReturn(404); // Example error code

        IOException thrown = assertThrows(IOException.class, () -> restaurantRepository.getRestaurantDataFromApi(), "IOException should be thrown for unsuccessful HTTP response");

        assertTrue(thrown.getMessage().contains("Unexpected response code: 404"), "Error message should contain the response code");
    }

    @Test
    void getRestaurantDataFromApi_nullResponseBody() {
        when(mockResponse.isSuccessful()).thenReturn(true);
        when(mockResponse.body()).thenReturn(null); // Simulate null body

        IOException thrown = assertThrows(IOException.class, restaurantRepository::getRestaurantDataFromApi, "IOException should be thrown for null response body");

        assertEquals("No response body received", thrown.getMessage(), "Error message should indicate no response body");
    }

    @Test
    void getRestaurantDataFromApi_missingRestaurantsNode() {
        when(mockResponse.isSuccessful()).thenReturn(true);
        try {
            when(mockResponseBody.string()).thenReturn(MISSING_RESTAURANTS_JSON_RESPONSE);
        } catch (IOException e) {
            fail("IOException should not occur when setting mock response body string.");
        }

        IOException thrown = assertThrows(IOException.class, () -> {
            restaurantRepository.getRestaurantDataFromApi();
        }, "IOException should be thrown for missing 'restaurants' node");

        assertTrue(thrown.getMessage().contains("Invalid response format: missing or invalid 'restaurants' array"),
                "Error message should indicate missing or invalid 'restaurants' array");
    }

    @Test
    void getRestaurantDataFromApi_restaurantsNodeNotArray() {
        when(mockResponse.isSuccessful()).thenReturn(true);
        try {
            when(mockResponseBody.string()).thenReturn(NON_ARRAY_RESTAURANTS_JSON_RESPONSE);
        } catch (IOException e) {
            fail("IOException should not occur when setting mock response body string.");
        }

        IOException thrown = assertThrows(IOException.class, restaurantRepository::getRestaurantDataFromApi, "IOException should be thrown when 'restaurants' node is not an array");

        assertTrue(thrown.getMessage().contains("Invalid response format: missing or invalid 'restaurants' array"),
                "Error message should indicate missing or invalid 'restaurants' array");
    }

    @Test
    void getRestaurantDataFromApi_malformedJson() {
        when(mockResponse.isSuccessful()).thenReturn(true);
        try {
            when(mockResponseBody.string()).thenReturn(MALFORMED_JSON_RESPONSE);
        } catch (IOException e) {
            fail("IOException should not occur when setting mock response body string.");
        }

        assertThrows(IOException.class, restaurantRepository::getRestaurantDataFromApi, "IOException should be thrown for malformed JSON response");
    }

    @Test
    void getRestaurantDataFromApi_networkError() throws IOException {
        when(mockCall.execute()).thenThrow(new IOException("Simulated network error"));

        IOException thrown = assertThrows(IOException.class, restaurantRepository::getRestaurantDataFromApi, "IOException should be thrown for network errors");

        assertEquals("Simulated network error", thrown.getMessage(), "Error message should match the simulated network error");
    }
}
