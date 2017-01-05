package se07.smart_ble.Serializable;

import java.io.Serializable;

import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.bleLockDevice;

/**
 * Created by QuanLM on 07-Dec-16.
 */
public class mySerializable implements Serializable {

    private static bleLockDevice dLock;
    private static LockData lockData;
    private static UserData userData;
    //private bleLockService mService;

    public mySerializable(bleLockDevice _dLock) {
        this.dLock = _dLock;
        lockData = new LockData();
        userData = new UserData();
    }

    public mySerializable() {
        dLock = new bleLockDevice();
        lockData = new LockData();
        userData = new UserData();
    };

    public bleLockDevice getLOCK(){
        return dLock;
    }
    public LockData getLockData() {
        return lockData;
    }
    public UserData getUserData() {
        return userData;
    }
    public void setBleLockDevice (bleLockDevice _bleLock) {
        this.dLock = _bleLock;
    }
    public void setLockData(LockData _lockData) {
        this.lockData = _lockData;
    }
    public void setUserData(UserData _userData) {
        this.userData = _userData;
    }
}
