package au.com.eatclub.repository;

import au.com.eatclub.model.Restaurant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.util.List;

/**
 * Repository class responsible for fetching restaurant data from an external API.
 * This class provides thread-safe access to restaurant data by making HTTP requests
 * to a predefined API endpoint. It handles the retrieval and deserialization of
 * restaurant data into {@link au.com.eatclub.model.Restaurant} objects.
 * it's marked as {@code @ThreadSafe} to ensure safe concurrent access.
 */
@ThreadSafe
public class RestaurantRepository {

    public static final String API_URL = "https://eccdn.com.au/misc/challengedata.json";
    private ObjectMapper objectMapper = new ObjectMapper();
    private OkHttpClient httpClient = new OkHttpClient();

    public List<Restaurant> getRestaurantDataFromApi() throws IOException {
        Request request = new Request.Builder()
                .url(API_URL)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response code: " + response.code());
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("No response body received");
            }

            JsonNode rootNode = objectMapper.readTree(body.string());
            JsonNode restaurantsNode = rootNode.get("restaurants");

            if (restaurantsNode == null || !restaurantsNode.isArray()) {
                throw new IOException("Invalid response format: missing or invalid 'restaurants' array");
            }

            return objectMapper.treeToValue(
                    restaurantsNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Restaurant.class)
            );
        }
    }
}
