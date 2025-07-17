package au.com.eatclub.lambda;

import au.com.eatclub.model.DealPeakTime;
import au.com.eatclub.service.RestaurantDealService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.Map;

/**
 * AWS Lambda function handler for retrieving peak times of restaurant deals.
 * This handler processes API Gateway proxy requests to find the time ranges when
 * the highest number of restaurant deals are active.
 * 
 * <p>Example request: v1/restaurants/deals/peak-times
 * 
 * <p>Implements AWS Lambda's RequestHandler interface to process API Gateway proxy events.
 * Returns a list of DealPeakTime objects in JSON format, each containing a time range
 * and the count of active deals during that range.
 */
public class GetPeakTimeForDealsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LogManager.getLogger(GetPeakTimeForDealsHandler.class);
    private  RestaurantDealService service = new RestaurantDealService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            logger.info("Request received for {}", request.getPath());

            List<DealPeakTime> dealPeakTimes = service.findPeakTimeRange();
            String jsonResponse = objectMapper.writeValueAsString(dealPeakTimes);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(jsonResponse);

        } catch (Exception e) {
            logger.error("error occurred {}: " , e.getMessage(), e);
            return errorResponse("Internal server error", HttpStatusCode.INTERNAL_SERVER_ERROR);
        }
    }

    private APIGatewayProxyResponseEvent errorResponse(String message, int statusCode) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody("{\"error\":\"" + message + "\"}");
    }
}
