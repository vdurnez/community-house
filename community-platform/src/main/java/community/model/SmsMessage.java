package community.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonAutoDetect
public class SmsMessage {

    @JsonIgnore
    public LocalDateTime timestamp;
    public SmsTypeEnum type;
    public String body;
    public String uuid;
    public String fromPhone;
    public String toPhone;

    @JsonIgnore
    public UserData.UserAlert userAlert;

    public static enum SmsTypeEnum {
        send, receive;
    }

    public SmsMessage(SmsTypeEnum typeEnum) {
        timestamp = LocalDateTime.now();
        this.type = typeEnum;
    }

    public String getDate() {
        return timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }


    @Override
    public String toString() {
        return "SmsMessage{" +
                "timestamp=" + timestamp +
                ", type=" + type +
                ", body='" + body + '\'' +
                ", uuid='" + uuid + '\'' +
                ", fromPhone='" + fromPhone + '\'' +
                ", toPhone='" + toPhone + '\'' +
                '}';
    }
}
