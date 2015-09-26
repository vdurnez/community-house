package community.api;

import community.model.InternalLogic;
import community.model.UserData;
import community.model.UserUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/1/sensor")
public class SensorApi {

    private Logger logger = Logger.getLogger(SmsApi.class);

    @Autowired
    InternalLogic internalLogic;

    @RequestMapping(value = "/alert/{uid}", method = RequestMethod.POST)
    public void alert(@PathVariable("uid") String uid, @RequestBody AlertDTO alertDTO) throws IOException {
        logger.info("alert " + alertDTO);
        final UserData user = UserUtils.getSingleton().getUser(uid);
        user.addAlert(alertDTO.location, alertDTO.sensor, alertDTO.value);

        internalLogic.reactOnSensorAlert(user);
    }

    public static class AlertDTO {
        public String location;
        public String sensor;
        public int value;


        @Override
        public String toString() {
            return "AlertDTO{" +
                    "location='" + location + '\'' +
                    ", sensor='" + sensor + '\'' +
                    ", value=" + value +
                    '}';
        }
    }
}
