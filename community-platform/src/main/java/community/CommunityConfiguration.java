package community;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommunityConfiguration {
    @Value("${twilio.account.sid}")
    private String twilioAccountSid;

    @Value("${twilio.auth.token}")
    private String twilioAuthToken;

    @Value("${twilio.number}")
    private String twilioNumber;

    @Value("${twilio.fake:false}")
    private boolean twilioFake;

    public boolean isTwilioFake() {
        return twilioFake;
    }

    public String getTwilioAccountSid() {
        return twilioAccountSid;
    }

    public String getTwilioAuthToken() {
        return twilioAuthToken;
    }

    public String getTwilioNumber() {
        return twilioNumber;
    }
}
