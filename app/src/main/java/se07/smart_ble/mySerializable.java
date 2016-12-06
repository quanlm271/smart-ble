package se07.smart_ble;

import java.io.Serializable;

/**
 * Created by QuanLM on 07-Dec-16.
 */
public class mySerializable implements Serializable {

    private bleLockDevice mLock;
    private bleLockService mService;

    public mySerializable(bleLockDevice device){
        mLock = device;
    }

    public bleLockDevice getLOCK(){
        return mLock;
    }

}
