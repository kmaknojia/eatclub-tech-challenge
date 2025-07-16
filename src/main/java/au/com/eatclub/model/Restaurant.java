package au.com.eatclub.model;

import au.com.eatclub.serialization.TimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @JsonProperty("objectId")
    private String objectId;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("address1")
    private String address1;
    
    @JsonProperty("suburb")
    private String suburb;
    
    @JsonProperty("cuisines")
    private List<String> cuisines;
    
    @JsonProperty("imageLink")
    private String imageLink;
    
    @JsonProperty("open")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime open;
    
    @JsonProperty("close")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime close;
    
    @JsonProperty("deals")
    private List<Deal> deals;
}
