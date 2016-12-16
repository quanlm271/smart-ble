package se07.smart_ble.Serializable;

import java.io.Serializable;
import java.util.ArrayList;

import se07.smart_ble.Models.UserData;

/**
 * Created by SangPham on 12/16/2016.
 */

public class SerializableUserData implements Serializable {
    private static UserData mUserData;

    public SerializableUserData(UserData userData) {
        this.mUserData = userData;
    }

    public UserData getUserData() {
        return mUserData;
    }
}
