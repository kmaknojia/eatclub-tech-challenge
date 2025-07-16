package au.com.eatclub.model;

import au.com.eatclub.serialization.TimeSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
public class DealPeakTime {

    @JsonProperty("peakTimeStart")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime peakTimeStart;

    @JsonProperty("peakTimeEnd")
    @JsonSerialize(using = TimeSerializer.class)
    private LocalTime peakTimeEnd;

}
