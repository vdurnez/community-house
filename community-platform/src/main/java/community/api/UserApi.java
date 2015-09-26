package community.api;

import community.model.UserData;
import community.model.UserUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/1/user")
public class UserApi {

    @RequestMapping(value = "/{uid}", method = RequestMethod.GET)
    public UserData userData(@PathVariable("uid") String uid) throws IOException {
        return UserUtils.getSingleton().getUser(uid);
    }
}
