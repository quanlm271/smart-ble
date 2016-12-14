package se07.smart_ble.Serializable;

import java.io.Serializable;
import java.util.List;

import se07.smart_ble.LockData;

/**
 * Created by SangPham on 12/13/2016.
 */

public class SerializableListLockData implements Serializable {
    private static List<LockData> mListLockData;

    public SerializableListLockData(List<LockData> listLockData){
        this.mListLockData = listLockData;
    }

    public List<LockData> getListLockData(){
        return this.mListLockData;
    }
}
