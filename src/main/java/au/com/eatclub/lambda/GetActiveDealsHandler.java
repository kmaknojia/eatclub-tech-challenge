package au.com.eatclub.lambda;

import au.com.eatclub.model.ActiveDeal;
import au.com.eatclub.service.RestaurantDealService;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.amazon.awssdk.http.HttpStatusCode;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

/**
 * AWS Lambda function handler for retrieving active restaurant deals at a specific time.
 * This handler processes API Gateway proxy requests containing a 'timeOfDay' query parameter
 * and returns a list of active deals available at that time.
 * 
 * <p>Example request: v1/restaurants/deals/active?timeOfDay=6:30PM
 * 
 * <p>Implements AWS Lambda's RequestHandler interface to process API Gateway proxy events.
 * Returns responses in JSON format with appropriate HTTP status codes.
 */
public class GetActiveDealsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger logger = LogManager.getLogger(GetActiveDealsHandler.class);
    private  RestaurantDealService service = new RestaurantDealService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    public final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mma");

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
        try {
            logger.info("Request received for {} with parameter{}", request.getPath(), request.getQueryStringParameters());
            String timeOfDay = request.getQueryStringParameters().get("timeOfDay");
            if (timeOfDay == null || timeOfDay.isEmpty()) {
                logger.error("Missing required parameter: timeOfDay");
                return errorResponse("Missing required parameter: timeOfDay", 400);
            }
            LocalTime time = convertToLocalTime(timeOfDay);
            List<ActiveDeal> activeDeals = service.getAllActiveDealsAtTime(time);
            String jsonResponse = objectMapper.writeValueAsString(activeDeals);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(jsonResponse);

        } catch (IllegalArgumentException illegalArgumentException) {
            return errorResponse(illegalArgumentException.getMessage(), 400);
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

    private LocalTime convertToLocalTime(String timeOfDay) {
        try {
            java.util.Date date = simpleDateFormat.parse(timeOfDay);
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

        } catch (Exception e) {
            logger.error("Error parsing timeOfDay: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid time format. Use format: h:mma");
        }
    }
}

