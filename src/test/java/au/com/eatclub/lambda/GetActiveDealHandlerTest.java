package au.com.eatclub.lambda;

import au.com.eatclub.model.ActiveDeal;
import au.com.eatclub.service.RestaurantDealService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GetActiveDealHandlerTest {

    @Mock
    private RestaurantDealService restaurantService;
    
    @Mock
    private Context mockContext;

    @Mock
    private LambdaLogger mockLogger;

    private ObjectMapper objectMapper;

    @InjectMocks
    private GetActiveDealsHandler handler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        when(mockContext.getLogger()).thenReturn(mockLogger);
        doNothing().when(mockLogger).log(any(String.class));
    }

    @Test
    void testValidTimeWithActiveDeals() throws Exception {
        ActiveDeal mockDeal = new ActiveDeal();
        mockDeal.setRestaurantObjectId("123");
        mockDeal.setRestaurantName("Test Restaurant");
        mockDeal.setDealObjectId("456");
        mockDeal.setDiscount("50");
        
        List<ActiveDeal> mockDeals = Collections.singletonList(mockDeal);
        
        when(restaurantService.getAllActiveDealsAtTime(any(LocalTime.class))).thenReturn(mockDeals);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of("timeOfDay", "10:30am"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        
        List<ActiveDeal> responseDeals = objectMapper.readValue(
            response.getBody(), 
            objectMapper.getTypeFactory().constructCollectionType(List.class, ActiveDeal.class)
        );
        assertEquals(1, responseDeals.size());
        assertEquals("123", responseDeals.get(0).getRestaurantObjectId());
    }

    @Test
    void testValidTimeNoActiveDeals() throws Exception {
        when(restaurantService.getAllActiveDealsAtTime(any(LocalTime.class))).thenReturn(Collections.emptyList());

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of("timeOfDay", "10:30am"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        
        List<ActiveDeal> responseDeals = objectMapper.readValue(
            response.getBody(), 
            objectMapper.getTypeFactory().constructCollectionType(List.class, ActiveDeal.class)
        );
        assertTrue(responseDeals.isEmpty());
    }

    @Test
    void testMissingTimeParameter() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Collections.emptyMap());

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);
        assertEquals(400, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("Missing required parameter: timeOfDay"));
    }

    @Test
    void testInvalidTimeFormat() {

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of("timeOfDay", "10:00"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);
        assertEquals(400, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("Invalid time format. Use format: h:mma"));
    }

    @Test
    void testServiceException() throws Exception {
        when(restaurantService.getAllActiveDealsAtTime(any(LocalTime.class))).thenThrow(new RuntimeException("Test error"));

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setQueryStringParameters(Map.of("timeOfDay", "10:30am"));

        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);
        assertEquals(500, response.getStatusCode());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertTrue(response.getBody().contains("Internal server error"));
    }
}
