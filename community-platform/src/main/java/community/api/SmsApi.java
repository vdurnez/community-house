package community.api;

import community.model.InternalLogic;
import community.model.SmsMessage;
import community.model.UserData;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/1/sms")
public class SmsApi {

    @Autowired
    private InternalLogic internalLogic;

    private Logger logger = Logger.getLogger(SmsApi.class);

    @RequestMapping(value = "/reply", method = RequestMethod.GET)
    public void receiveReply(@RequestParam Map<String, String> allRequestParams) throws IOException {
        JSONObject params = new JSONObject(allRequestParams);
        logger.info("receive SMS " + params.toJSONString());

        String body = allRequestParams.get("Body");
        String from = allRequestParams.get("From");
        String to = allRequestParams.get("To");
        String smsId = allRequestParams.get("SmsSid");

        SmsMessage smsMessage = new SmsMessage(SmsMessage.SmsTypeEnum.receive);
        smsMessage.fromPhone = from;
        smsMessage.toPhone = to;
        smsMessage.body = body;
        smsMessage.uuid = smsId;

        UserData userData = internalLogic.findUserByPhoneAndSmsId(from, smsId);

        if (userData == null) {
            logger.error("failure : unknown phoneNumber=" + from + " - drop SmsId=" + smsId + ", with body=" + body);
            return;
        }

        userData.smsMessages.add(smsMessage);

        internalLogic.reactOnUserResponse(userData, smsMessage);
    }
}
