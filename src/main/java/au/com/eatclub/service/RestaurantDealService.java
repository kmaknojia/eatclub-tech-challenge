package au.com.eatclub.service;

import au.com.eatclub.mapper.DealMapper;
import au.com.eatclub.model.ActiveDeal;
import au.com.eatclub.model.Deal;
import au.com.eatclub.model.DealPeakTime;
import au.com.eatclub.model.Restaurant;
import au.com.eatclub.repository.RestaurantRepository;

import javax.annotation.concurrent.ThreadSafe;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for managing restaurant deals and their availability.
 * Determines active deals at specific times and identifies peak periods with the highest
 * concentration of active deals across all restaurants.
 */
@ThreadSafe
public class RestaurantDealService {
    private static final int MINUTES_IN_HOUR = 60;
    public static final int DAY_TOTAL_MINUTES = 24 * MINUTES_IN_HOUR;
    private  RestaurantRepository repository = new RestaurantRepository();
    private static final DealMapper mapper = DealMapper.INSTANCE;

    /**
     * Retrieves all active deals across all restaurants at the specified time.
     *
     * Approach:
     * 1. Fetches all restaurants and their deals from the repository
     * 2. For each deal, determines its active time window:
     *    - Uses deal-specific times if available
     *    - Falls back to restaurant's operating hours if deal times are not specified
     * 3. Checks if the specified time falls within the active window
     * 4. Maps and collects all active deals into the result list
     *
     */
    public List<ActiveDeal> getAllActiveDealsAtTime(LocalTime time) throws IOException {
        List<Restaurant> allRestaurants = repository.getRestaurantDataFromApi();
        List<ActiveDeal> activeDeals = new ArrayList<>();

        // Check each restaurant's deals for active status at the given time
        for (Restaurant restaurant : allRestaurants) {
            if (restaurant.getDeals() == null) {
                continue;
            }

            for (Deal deal : restaurant.getDeals()) {
                // Use deal-specific times if available, otherwise use restaurant's operating hours
                LocalTime dealStartTime = deal.getOpen() != null ? deal.getOpen() : deal.getStart();
                LocalTime dealEndTime = deal.getClose() != null ? deal.getClose() : deal.getEnd();

                // Fallback to restaurant's operating hours if deal times are not specified
                if (dealStartTime == null || dealEndTime == null) {
                    dealStartTime = restaurant.getOpen();
                    dealEndTime = restaurant.getClose();
                }

                // Check if the deal is active at the specified time
                if (isActiveDeal(time, restaurant.getOpen(), restaurant.getClose(), dealStartTime, dealEndTime)) {
                    ActiveDeal resultDeal = mapper.mapActiveDeal(restaurant, deal);
                    activeDeals.add(resultDeal);
                }
            }
        }

        return activeDeals;
    }

    private boolean isActiveDeal(LocalTime dealRequestTime, LocalTime restaurantOpenTime, LocalTime restaurantCloseTime,
                                 LocalTime dealStartTime, LocalTime dealEndTime) {

        // Not handling overnight hours I-e restaurant/deal finishes next day
        if (!hasTimeOverlap(restaurantOpenTime, restaurantCloseTime, dealStartTime, dealEndTime)) {
            return false;
        }

        LocalTime actualDealStartTime = restaurantOpenTime.compareTo(dealStartTime) > 0 ? restaurantOpenTime : dealStartTime;
        LocalTime actualDealEndTime = restaurantCloseTime.compareTo(dealEndTime) < 0 ? restaurantCloseTime : dealEndTime;

        return (dealRequestTime.compareTo(actualDealStartTime) >= 0 && dealRequestTime.compareTo(actualDealEndTime) <= 0);
    }

    /**
     * Identifies time periods with the highest concentration of active deals across all restaurants.
     * 
     * Approach:
     * 1. Tracks deal activity per minute across a 24-hour period
     * 2. For each deal, marks its active minutes within the restaurant's operating hours
     * 3. Finds the maximum number of overlapping deals
     * 4. Identifies all continuous time ranges with this maximum overlap
     * 
     * @return List of peak time ranges with the highest deal activity
     */
    public List<DealPeakTime> findPeakTimeRange() throws IOException {
        List<Restaurant> restaurants = repository.getRestaurantDataFromApi();
        int[] dealCounts = new int[DAY_TOTAL_MINUTES]; // Tracks concurrent deals per minute
        List<DealPeakTime> peakTimes = new ArrayList<>();

        // Process each restaurant's deals to build the minute-by-minute deal count
        for (Restaurant restaurant : restaurants) {
            if (restaurant.getDeals() == null) {
                continue; // Skip restaurants with no deals
            }

            for (Deal deal : restaurant.getDeals()) {
                // Use deal-specific times if available, otherwise fall back to restaurant hours
                LocalTime dealStartTime = deal.getOpen() != null ? deal.getOpen() : deal.getStart();
                LocalTime dealEndTime = deal.getClose() != null ? deal.getClose() : deal.getEnd();

                // If deal times aren't specified, use restaurant's operating hours
                if (dealStartTime == null || dealEndTime == null) {
                    dealStartTime = restaurant.getOpen();
                    dealEndTime = restaurant.getClose();
                }

                // Mark active minutes for this deal
                markDealTime(dealCounts, restaurant.getOpen(),
                        restaurant.getClose(),
                        dealStartTime, dealEndTime);
            }
        }

        // Find the maximum number of overlapping deals at any minute
        int maxDeals = Arrays.stream(dealCounts).max().orElse(0);
        if (maxDeals == 0) {
            return peakTimes; // No active deals found
        }

        // Identify all time ranges with the maximum number of concurrent deals
        boolean inPeak = false;
        int rangeStart = -1;

        // Scan through each minute to find peak ranges
        for (int i = 0; i <= dealCounts.length; i++) {
            int current = (i < dealCounts.length) ? dealCounts[i] : 0;

            if ((current == maxDeals) && !inPeak) {
                // Start of a new peak range
                inPeak = true;
                rangeStart = i;
            } else if (current != maxDeals && inPeak) {
                // End of the current peak range
                int endMinute = i - 1;
                peakTimes.add(new DealPeakTime(
                        LocalTime.of(rangeStart / MINUTES_IN_HOUR, rangeStart % MINUTES_IN_HOUR),
                        LocalTime.of(endMinute / MINUTES_IN_HOUR, endMinute % MINUTES_IN_HOUR)
                ));
                inPeak = false;
            }
        }
        return peakTimes;
    }

    private void markDealTime(int[] dealCounts, LocalTime restaurantOpenTime, LocalTime restaurantCloseTime,
                              LocalTime dealStartTime, LocalTime dealEndTime) {
        int restOpenMin = restaurantOpenTime.getHour() * MINUTES_IN_HOUR + restaurantOpenTime.getMinute();
        int restCloseMin = restaurantCloseTime.getHour() * MINUTES_IN_HOUR + restaurantCloseTime.getMinute();
        int dealStartMin = dealStartTime.getHour() * MINUTES_IN_HOUR + dealStartTime.getMinute();
        int dealEndMin = dealEndTime.getHour() * MINUTES_IN_HOUR + dealEndTime.getMinute();

        // Not handling overnight hours i-e restaurant/deal finishes next day
        if (!hasTimeOverlap(restOpenMin, restCloseMin, dealStartMin, dealEndMin)) {
            return;
        }

        // Adjust for restaurant hours
        dealStartMin = Math.max(dealStartMin, restOpenMin);
        dealEndMin = Math.min(dealEndMin, restCloseMin);

        if (dealStartMin <= dealEndMin) {
            for (int i = dealStartMin; i <= dealEndMin; i++) {
                dealCounts[i]++;
            }
        }
    }

    private boolean hasTimeOverlap(int restOpenMin, int restCloseMin, int dealStartMin, int dealEndMin) {
        return dealStartMin <= restCloseMin && dealEndMin >= restOpenMin;
    }

    private boolean hasTimeOverlap(LocalTime restOpenTime, LocalTime restCloseTime,  LocalTime dealStartTime, LocalTime dealEndTime) {
        return dealStartTime.compareTo(restCloseTime) <= 0 && dealEndTime.compareTo(restOpenTime) >= 0;
    }

}