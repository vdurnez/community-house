package community.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@JsonAutoDetect
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserData {

    public String uid;
    public String phoneNumber;
    public List<String> neighborPhoneNumbers = new ArrayList<>();

    public List<Event> events = new ArrayList<>();
    public List<SmsMessage> smsMessages = new ArrayList<>();

    public List<UserAlert> activeAlerts = new ArrayList<>();
    public List<UserAlert> pastAlerts = new ArrayList<>();

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
    public Event getLastEvent() {
        if (events.size() == 0)
            return null;
        return events.get(events.size() - 1);
    }

    public void closeAlert(UserAlert alert) {
        alert.end = LocalDateTime.now();
        this.activeAlerts.remove(alert);
        this.pastAlerts.add(alert);
    }

    public UserAlert findAlert(SmsMessage message) {
        if (message.userAlert != null)
            return message.userAlert;

        // assumption : smsId within twilio provider track conversation, but not sure
        for (SmsMessage smsMessage : smsMessages) {
            if (smsMessage == message)
                continue;
            if (smsMessage.uuid != null && smsMessage.uuid.equalsIgnoreCase(message.uuid) && smsMessage.userAlert != null) {
                message.userAlert = smsMessage.userAlert;
                return message.userAlert;
            }
        }
        // fallback : take latest alert
        return activeAlerts.get(activeAlerts.size() - 1);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public static class UserAlert {
        @JsonIgnore
        public UserData userData;

        @JsonIgnore
        public LocalDateTime timestamp;

        @JsonIgnore
        public LocalDateTime update;

        @JsonIgnore
        public LocalDateTime end;

        public String endDescription;

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

        public String getEndDate() {
            if (end != null)
                return end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            else return null;
        }

        public String getUpdate() {
            if (update != null)
                return update.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            else return null;
        }

        public String getStartDate() {
            return timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }


    }

    public static enum AlertLevelEnum {
        owner, all, police;
    }

}
