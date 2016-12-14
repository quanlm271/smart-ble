package se07.smart_ble;

import java.io.Serializable;

/**
 * Created by QuanLM on 07-Dec-16.
 */
public class mySerializable implements Serializable {

    private static bleLockDevice dLock;
    //private bleLockService mService;

    public mySerializable(bleLockDevice lock){
        this.dLock = lock;
    }

    public bleLockDevice getLOCK(){
        return dLock;
    }

}
