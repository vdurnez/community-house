package community.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UserUtils {

    public static UserUtils Singleton;

    public Map<String, UserData> userDataMap = new HashMap<>();

    public static UserUtils getSingleton() {
        if (Singleton == null) {
            Singleton = new UserUtils();
        }

        return Singleton;
    }

    public UserData getUser(String uid) {
        UserData userData = userDataMap.get(uid);

        if (userData == null) {
            userData = new UserData(uid);
            userData.phoneNumber = "07793486734";
            userData.neighborPhoneNumbers.add("447472647105");

            userDataMap.put(uid, userData);
        }

        return userData;
    }

    public Set<String> getUsersId() {
        return userDataMap.keySet();
    }

}
