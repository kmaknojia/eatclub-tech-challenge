package au.com.eatclub.model;

import au.com.eatclub.serialization.TimeSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ActiveDeal {
    @JsonProperty("restaurantObjectId")
    private String restaurantObjectId;
    
    @JsonProperty("restaurantName")
    private String restaurantName;
    
    @JsonProperty("restaurantAddress1")
    private String restaurantAddress1;
    
    @JsonProperty("restaurantSuburb")
    private String restaurantSuburb;
    
    @JsonProperty("restaurantOpen")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime restaurantOpen;
    
    @JsonProperty("restaurantClose")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime restaurantClose;
    
    @JsonProperty("dealObjectId")
    private String dealObjectId;
    
    @JsonProperty("discount")
    private String discount;
    
    @JsonProperty("dineIn")
    private boolean dineIn;
    
    @JsonProperty("lightning")
    private boolean lightning;
    
    @JsonProperty("qtyLeft")
    private String qtyLeft;
}
