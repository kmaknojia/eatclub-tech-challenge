package au.com.eatclub.service;

import au.com.eatclub.model.ActiveDeal;
import au.com.eatclub.model.DealPeakTime;
import au.com.eatclub.model.Restaurant;
import au.com.eatclub.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestaurantDealServiceTest {

    @Mock
    private RestaurantRepository repository;

    @InjectMocks
    private RestaurantDealService service;

    private ObjectMapper objectMapper = new ObjectMapper();


    private List<Restaurant> testRestaurants;

    @BeforeEach
    void setUp() throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get("src/test/resources/data.json"))) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode restaurantsNode = rootNode.get("restaurants");
            testRestaurants = objectMapper.treeToValue(
                    restaurantsNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Restaurant.class)
            );
        }
    }

    private static Stream<Arguments> activeDealsTestData() {
        return Stream.of(
            Arguments.of(LocalTime.of(15, 0), 7),  // 3 PM - 7 active deals
            Arguments.of(LocalTime.of(18, 0), 9),  // 6 PM - 9 active deals
            Arguments.of(LocalTime.of(21, 0), 9)   // 9 PM - 9 active deals
        );
    }

    @ParameterizedTest
    @MethodSource("activeDealsTestData")
    void getAllActiveDealsAtTime_ShouldReturnExpectedNumberOfDeals(LocalTime testTime, int expectedDealCount) throws IOException {
        when(repository.getRestaurantDataFromApi()).thenReturn(testRestaurants);

        List<ActiveDeal> activeDeals = service.getAllActiveDealsAtTime(testTime);

        assertEquals(expectedDealCount, activeDeals.size(), 
            String.format("Expected %d active deals at %s, but found %d", 
                expectedDealCount, testTime, activeDeals.size()));
    }

    @Test
    void getAllActiveDealsAtTime_ShouldReturnEmptyList_ForTimeWithNoActiveDeals() throws IOException {
        // Arrange
        LocalTime testTime = LocalTime.of(2, 0);
        when(repository.getRestaurantDataFromApi()).thenReturn(testRestaurants);

        // Act
        List<ActiveDeal> activeDeals = service.getAllActiveDealsAtTime(testTime);

        // Assert
        assertNotNull(activeDeals);
        assertTrue(activeDeals.isEmpty());
    }

    @Test
    void findPeakTimeRange_ShouldReturnPeakTimes() throws IOException {
        when(repository.getRestaurantDataFromApi()).thenReturn(testRestaurants);

        List<DealPeakTime> peakTimes = service.findPeakTimeRange();

        assertEquals(1, peakTimes.size());
        assertEquals(LocalTime.of(18, 0), peakTimes.get(0).getPeakTimeStart());
        assertEquals(LocalTime.of(21, 0), peakTimes.get(0).getPeakTimeEnd());
    }

}