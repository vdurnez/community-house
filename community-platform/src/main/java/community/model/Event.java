package community.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDateTime;

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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSensor() {
        return sensor;
    }

    public int getValue() {
        return value;
    }
}
