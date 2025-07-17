package au.com.eatclub.lambda;

import au.com.eatclub.model.DealPeakTime;
import au.com.eatclub.serialization.TimeDeserializer;
import au.com.eatclub.serialization.TimeSerializer;
import au.com.eatclub.service.RestaurantDealService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class GetPeakTimeForDealsHandlerTest {

    @Mock
    private RestaurantDealService restaurantService;

    @Mock
    private Context mockContext;

    @Mock
    private LambdaLogger mockLogger;

    private ObjectMapper objectMapper;

    @InjectMocks
    private GetPeakTimeForDealsHandler handler;


    GetPeakTimeForDealsHandlerTest() {
        objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(LocalTime.class, new TimeDeserializer());
        module.addSerializer(LocalTime.class, new TimeSerializer());
        objectMapper.registerModule(module);
    }


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockContext.getLogger()).thenReturn(mockLogger);
        doNothing().when(mockLogger).log(any(String.class));
    }

    @Test
    void testValidDealPeakTime() throws Exception {
        DealPeakTime mockDeal = new DealPeakTime();
        mockDeal.setPeakTimeStart(LocalTime.of(10, 30));
        mockDeal.setPeakTimeEnd(LocalTime.of(11, 30));

        List<DealPeakTime> mockDeals = Collections.singletonList(mockDeal);

        when(restaurantService.findPeakTimeRange()).thenReturn(mockDeals);

        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        APIGatewayProxyResponseEvent response = handler.handleRequest(request, mockContext);

        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("application/json", response.getHeaders().get("Content-Type"));

        List<DealPeakTime> responseDeals = objectMapper.readValue(
                response.getBody(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, DealPeakTime.class)
        );
        assertEquals(1, responseDeals.size());
        assertEquals(LocalTime.of(10, 30), responseDeals.get(0).getPeakTimeStart());
        assertEquals(LocalTime.of(11, 30), responseDeals.get(0).getPeakTimeEnd());
    }

}