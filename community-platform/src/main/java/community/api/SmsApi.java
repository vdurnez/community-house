package community.api;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/1/sms")
public class SmsApi {

    private Logger logger = Logger.getLogger(SmsApi.class);

    @RequestMapping(value = "/reply", method = RequestMethod.GET)
    public void receiveReply(@RequestParam Map<String, String> allRequestParams) throws IOException {
        for (String key : allRequestParams.keySet()) {
            logger.info(key + "=" + allRequestParams.get(key));
        }

    }
}
