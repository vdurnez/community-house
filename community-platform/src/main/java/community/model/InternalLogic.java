package community.model;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import community.CommunityConfiguration;
import community.api.SmsApi;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class InternalLogic {

    private Logger logger = Logger.getLogger(SmsApi.class);

    @Autowired
    CommunityConfiguration communityConfiguration;

    private TwilioRestClient client;

    private Map<String, UserData> userDataMap = new HashMap<>();


    @PostConstruct
    public void init() {
        client = new TwilioRestClient(communityConfiguration.getTwilioAccountSid(),
                communityConfiguration.getTwilioAuthToken());
    }

    public UserData findUser(String uid) {
        UserData userData = userDataMap.get(uid);

        if (userData == null) {
            userData = new UserData(uid);
            userData.phoneNumber = "447793486734";
            userData.neighborPhoneNumbers.add("447472647105");

            userDataMap.put(uid, userData);
        }

        return userData;
    }

    public UserData findUserByPhoneAndSmsId(String phone, String smsId) {
        String phoneClean = phone.replaceAll("\\+", "").replaceAll("\\s", "");
        for (UserData userData : userDataMap.values()) {
            if (userData.phoneNumber.indexOf(phoneClean) >= 0) {
                return userData;
            }
            for (String neighborPhone : userData.neighborPhoneNumbers) {
                if (neighborPhone.indexOf(phoneClean) >= 0)
                    return userData;
            }
        }
        logger.warn("phone " + phone + " not recognized - phoneClean=" + phoneClean);
        return null;
    }

    public Set<String> getUsersId() {
        return userDataMap.keySet();
    }


    public UserData.UserAlert findLatestActiveAlert(UserData userData, String type) {
        for (UserData.UserAlert userAlert : userData.activeAlerts) {
            if (userAlert.sensor.equalsIgnoreCase(type)) {
                return userAlert;
            }
        }
        return null;
    }

    public UserData.UserAlert findLatestPastAlert(UserData userData, String type) {
        for (UserData.UserAlert userAlert : userData.pastAlerts) {
            if (userAlert.sensor.equalsIgnoreCase(type)) {
                return userAlert;
            }
        }
        return null;
    }


    public void reactOnSensorAlert(UserData userData) {
        Event lastEvent = userData.getLastEvent();
        if (lastEvent == null)
            return;

        String sensor = lastEvent.getSensor();

        UserData.UserAlert latestAlert = findLatestActiveAlert(userData, sensor);
        if (latestAlert != null) {
            logger.info("already active alert " + sensor + " no extra SMS");
            return;
        }

        // avoid sensor still alerting and UserAlert closed.
        LocalDateTime now = LocalDateTime.now();
        latestAlert = findLatestPastAlert(userData, sensor);
        if (latestAlert != null && now.minusSeconds(300).compareTo(latestAlert.update) < 0) {
            logger.info("closed alert " + sensor + " less than 5 minutes ago - no action");
            return;
        }

        if (sensor.equalsIgnoreCase("fire")) {
            latestAlert = new UserData.UserAlert(userData, sensor, lastEvent.location, UserData.AlertLevelEnum.all);
            userData.activeAlerts.add(0, latestAlert);

            SmsMessage ownerSms = sendSmsToOwner(userData, "alert at " + lastEvent.location + " : " + sensor + " ? please reply OK or HELP");
            ownerSms.userAlert = latestAlert;
            SmsMessage sendSmsToNeighbors = sendSmsToNeighbors(userData, "alert at Mr Smith " + lastEvent.location + ": " + sensor + "? please reply OK or HELP");
            sendSmsToNeighbors.userAlert = latestAlert;
        } else {
            latestAlert = new UserData.UserAlert(userData, sensor, lastEvent.location, UserData.AlertLevelEnum.owner);
            userData.activeAlerts.add(0, latestAlert);

            SmsMessage ownerSms = sendSmsToOwner(userData, "Unexpected " + sensor + " at " + lastEvent.location + ", please reply OK or HELP");
            ownerSms.userAlert = latestAlert;
        }
    }

    public void reactOnUserResponse(UserData user, SmsMessage smsMessage) {
        UserData.UserAlert userAlert = user.findAlert(smsMessage);

        String userAnswer = smsMessage.body;
        if (userAnswer.toLowerCase().startsWith("ok")) {
            user.closeAlert(userAlert);
            userAlert.endDescription = smsMessage.fromPhone + ": close alert";

            // TODO: if neighbor closes alert, send SMS to owner to reassure
        }

        if (userAnswer.toLowerCase().startsWith("help")) {
            // raise alert
            SmsMessage sendSmsToNeighbors = sendSmsToNeighbors(user, "alert at Mr Smith " + userAlert.location + ": " + userAlert.sensor + "? please reply OK or HELP");
            sendSmsToNeighbors.userAlert = userAlert;
            userAlert.currentLevel = UserData.AlertLevelEnum.all;
            userAlert.update = LocalDateTime.now();
        }

    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void manageUserInteraction() {
        LocalDateTime now = LocalDateTime.now();


        for (UserData user : userDataMap.values()) {
            List<UserData.UserAlert> toBeRemoved = new ArrayList<>();

            for (UserData.UserAlert userAlert : user.activeAlerts) {
                switch (userAlert.currentLevel) {
                    case owner:
                        if (now.minusSeconds(60).compareTo(userAlert.update) > 0) {
                            userAlert.currentLevel = UserData.AlertLevelEnum.all;
                            userAlert.update = LocalDateTime.now();
                            sendSmsToNeighbors(userAlert.userData, "alert at Mr Smith : " + userAlert.sensor + "? please answer OK or HELP");
                        }
                    case all:
                        if (now.minusSeconds(120).compareTo(userAlert.update) > 0) {
                            userAlert.currentLevel = UserData.AlertLevelEnum.police;
                            userAlert.update = LocalDateTime.now();
                            logger.fatal("Alert not managed since " + userAlert.timestamp + " => call police (FAKE)");
                        }
                    case police:
                        if (now.minusSeconds(600).compareTo(userAlert.update) > 0) {
                            userAlert.update = LocalDateTime.now();
                            logger.fatal("STOP police alert...");
                            toBeRemoved.add(userAlert);
                            userAlert.endDescription = "police: automatic stop";
                        }
                }
            }
            for (UserData.UserAlert userAlert : toBeRemoved) {
                user.closeAlert(userAlert);
            }
        }
    }


    public SmsMessage sendSmsToOwner(UserData userData, String message) {
        SmsMessage smsMessage = new SmsMessage(SmsMessage.SmsTypeEnum.send);
        smsMessage.fromPhone = communityConfiguration.getTwilioNumber();
        smsMessage.toPhone = userData.phoneNumber;
        smsMessage.body = message;

        userData.smsMessages.add(smsMessage);

        sendSms(smsMessage);

        return smsMessage;
    }

    public SmsMessage sendSmsToNeighbors(UserData userData, String message) {
        SmsMessage smsMessage = new SmsMessage(SmsMessage.SmsTypeEnum.send);
        smsMessage.fromPhone = communityConfiguration.getTwilioNumber();
        smsMessage.toPhone = userData.neighborPhoneNumbers.get(0);
        smsMessage.body = message;

        userData.smsMessages.add(smsMessage);

        sendSms(smsMessage);

        return smsMessage;
    }

    private void sendSms(SmsMessage smsMessage) {
        // Build the parameters
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("From", smsMessage.fromPhone));
        params.add(new BasicNameValuePair("To", smsMessage.toPhone));
        params.add(new BasicNameValuePair("Body", smsMessage.body));

        if (communityConfiguration.isTwilioFake()) {
            logger.info("FAKE SMS provider : do NOT send SMS " + params);
            smsMessage.uuid = "fake_smsid";
            return;
        }

        MessageFactory messageFactory = client.getAccount().getMessageFactory();
        Message message;
        try {
            message = messageFactory.create(params);
            smsMessage.uuid = message.getSid();
            logger.info("SMS send " + smsMessage);
            logger.info("SMS answer.json=" + message.toJSON());

        } catch (TwilioRestException e) {
            logger.error("failure when sending " + smsMessage + " exception=" + e);
        }
    }
}
