package community.model;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import community.api.SmsApi;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class InternalLogic {

    private Logger logger = Logger.getLogger(SmsApi.class);
    public final String ACCOUNT_SID = "AC85712b6c7f11371db19fdaf237350a9d";
    public final String AUTH_TOKEN = "7c8636348b19ea330a88e5e8ec0c4e25";
    public final String twilioNumber = "441274451669";
    TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

    public UserData.UserAlert findLatestAlert(UserData userData, String type) {
        for (UserData.UserAlert userAlert : userData.activeAlerts) {
            if (userAlert.sensor.equalsIgnoreCase(type)) {
                return userAlert;
            }
        }
        return null;
    }


    public void reactOnSensorAlert(UserData userData) {
        Event lastAlert = userData.getLastAlert();
        if (lastAlert == null)
            return;

        String sensor = lastAlert.getSensor();

        if (sensor.equalsIgnoreCase("fire")) {
            sendSmsToOwner(userData, "alert at " + lastAlert.location + " : " + sensor + " ? please reply OK or HELP");
            sendSmsToNeighbors(userData, "alert at Mr Smith " + lastAlert.location + ": " + sensor + "? please reply OK or HELP");

            if (findLatestAlert(userData, sensor) == null) {
                userData.activeAlerts.add(0, new UserData.UserAlert(userData, sensor, lastAlert.location, UserData.AlertLevelEnum.all));
            }
        } else {
            sendSmsToOwner(userData, "Unexpected " + sensor + " at " + lastAlert.location + ", please reply OK or HELP");
            if (findLatestAlert(userData, sensor) == null) {
                userData.activeAlerts.add(0, new UserData.UserAlert(userData, sensor, lastAlert.location, UserData.AlertLevelEnum.owner));
            }
        }
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void manageUserInteraction() {
        LocalDateTime now = LocalDateTime.now();


        for (String userId : UserUtils.getSingleton().getUsersId()) {
            UserData user = UserUtils.getSingleton().getUser(userId);
            List<UserData.UserAlert> toBeRemoved = new ArrayList<>();

            for (UserData.UserAlert userAlert : user.activeAlerts) {
                switch (userAlert.currentLevel) {
                    case owner:
                        if (now.minusSeconds(30).compareTo(userAlert.update) > 0) {
                            userAlert.currentLevel = UserData.AlertLevelEnum.all;
                            userAlert.update = LocalDateTime.now();
                            sendSmsToNeighbors(userAlert.userData, "alert at Mr Smith : " + userAlert.sensor + "? please answer OK or HELP");
                        }
                    case all:
                        if (now.minusSeconds(60).compareTo(userAlert.update) > 0) {
                            userAlert.currentLevel = UserData.AlertLevelEnum.police;
                            userAlert.update = LocalDateTime.now();
                            logger.fatal("Alert not managed since " + userAlert.timestamp + " => call police");
                        }
                    case police:
                        if (now.minusSeconds(600).compareTo(userAlert.update) > 0) {
                            userAlert.update = LocalDateTime.now();
                            logger.fatal("STOP police alert...");
                            toBeRemoved.add(userAlert);
                            userAlert.userData.addAlert("police_stop", userAlert.sensor, 0);
                        }
                }
            }
            for (UserData.UserAlert userAlert : toBeRemoved) {
                user.activeAlerts.remove(userAlert);
            }
        }
    }


    public void sendSmsToOwner(UserData userData, String message) {
        SmsMessage smsMessage = new SmsMessage(SmsMessage.SmsTypeEnum.send);
        smsMessage.fromPhone = twilioNumber;
        smsMessage.toPhone = userData.phoneNumber;
        smsMessage.body = message;

        userData.smsMessages.add(smsMessage);

        sendSms(smsMessage);
    }

    public void sendSmsToNeighbors(UserData userData, String message) {
        SmsMessage smsMessage = new SmsMessage(SmsMessage.SmsTypeEnum.send);
        smsMessage.fromPhone = twilioNumber;
        smsMessage.toPhone = userData.neighborPhoneNumbers.get(0);
        smsMessage.body = message;

        userData.smsMessages.add(smsMessage);

        sendSms(smsMessage);
    }

    private void sendSms(SmsMessage smsMessage) {
        // Build the parameters
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("From", smsMessage.fromPhone));
        params.add(new BasicNameValuePair("To", smsMessage.toPhone));
        params.add(new BasicNameValuePair("Body", smsMessage.body));

        MessageFactory messageFactory = client.getAccount().getMessageFactory();
        Message message;
        try {
            message = messageFactory.create(params);
            smsMessage.uuid = message.getSid();
            logger.info("send " + smsMessage);
        } catch (TwilioRestException e) {
            logger.error("failure when sending " + smsMessage + " exception=" + e);
        }
    }
}
