package community.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserData {

    public String uid;
    public String phoneNumber;
    public List<String> neighborPhoneNumbers = new ArrayList<>();

    public List<Event> events = new ArrayList<>();
    public List<SmsMessage> smsMessages = new ArrayList<>();

    public List<UserAlert> activeAlerts = new ArrayList<>();

    public UserData(String uid) {
        this.uid = uid;
    }

    public void addAlert(String location, String sensor, int value) {
        this.events.add(new Event(location, sensor, value));
    }

    public List<Event> getEvents() {
        return events;
    }

    @JsonIgnore
    public Event getLastAlert() {
        if (events.size() == 0)
            return null;
        return events.get(events.size() - 1);
    }

    public static class UserAlert {
        public UserData userData;
        public LocalDateTime timestamp;
        public LocalDateTime update;
        public String sensor;
        public String location;
        public AlertLevelEnum currentLevel;

        public UserAlert(UserData userData, String type, String location, AlertLevelEnum level) {
            this.timestamp = LocalDateTime.now();
            update = this.timestamp;

            this.userData = userData;
            this.sensor = type;
            this.location = location;
            this.currentLevel = level;
        }
    }

    public static enum AlertLevelEnum {
        owner, all, police;
    }

}
