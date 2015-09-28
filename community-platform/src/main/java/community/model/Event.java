package community.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonAutoDetect
public class Event {
    private LocalDateTime timestamp;
    public String location;
    public String sensor;
    public int value;

    public Event(String location, String sensor, int value) {
        timestamp = LocalDateTime.now();

        this.location = location;
        this.sensor = sensor;
        this.value = value;
    }

    @JsonIgnore
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDate() {
        return timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    public String getSensor() {
        return sensor;
    }

    public int getValue() {
        return value;
    }
}
