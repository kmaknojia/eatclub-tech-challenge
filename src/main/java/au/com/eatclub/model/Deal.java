package au.com.eatclub.model;

import au.com.eatclub.serialization.TimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalTime;

@Data
public class Deal {
    @JsonProperty("objectId")
    private String objectId;
    
    @JsonProperty("discount")
    private String discount;
    
    @JsonProperty("dineIn")
    private boolean dineIn;
    
    @JsonProperty("lightning")
    private boolean lightning;
    
    @JsonProperty("open")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime open;
    
    @JsonProperty("close")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime close;
    
    @JsonProperty("start")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime start;
    
    @JsonProperty("end")
    @JsonDeserialize(using = TimeDeserializer.class)
    private LocalTime end;
    
    @JsonProperty("qtyLeft")
    private String qtyLeft;

}
