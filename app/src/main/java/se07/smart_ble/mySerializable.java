package se07.smart_ble;

import java.io.Serializable;

/**
 * Created by QuanLM on 07-Dec-16.
 */
public class mySerializable implements Serializable {

    private static LockData dLock;
    //private bleLockService mService;

    public mySerializable(LockData lock){
        this.dLock = lock;
    }

    public LockData getLOCK(){
        return dLock;
    }

}
