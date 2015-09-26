package community.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.time.LocalDateTime;

@JsonAutoDetect
public class SmsMessage {

    public LocalDateTime timestamp;
    public SmsTypeEnum type;
    public String body;
    public String uuid;
    public String fromPhone;
    public String toPhone;

    public static enum SmsTypeEnum {
        send, receive;
    }

    public SmsMessage(SmsTypeEnum typeEnum) {
        timestamp = LocalDateTime.now();
        this.type = typeEnum;
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
