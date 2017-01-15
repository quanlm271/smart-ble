package se07.smart_ble.Models;

import android.util.Log;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import se07.smart_ble.API.Common;

/**
 * Created by QuanLM on 10-Dec-16.
 */
public class LockData {
    private final int keySize = 48; // 6 * 8

    private int mLockId;
    private String mName;
    private String mMAC;
    private String mPin;
    private byte[] mSessionKey;

    public boolean IsInBound = false;

    public LockData(int _lockId, String _name, String _mac){
        this.mLockId = _lockId;
        this.mName = _name;
        this.mMAC = _mac;
        this.mPin = "";
        this.mSessionKey = Common.GenerateKeyAES(keySize);
    }

    public LockData(String _name, String _mac){
        this.mLockId = -1;
        this.mName = _name;
        this.mMAC = _mac;
        this.mPin = "";
        this.mSessionKey = Common.GenerateKeyAES(keySize);
    }

    public LockData () {
        this.mLockId = -1;
        this.mName = "";
        this.mMAC = "";
        this.mPin = "";
        this.mSessionKey = Common.GenerateKeyAES(keySize);
    }

    // Get Methods
    public int getLockId () {return mLockId;}
    public String get_mName(){return mName;}
    public String get_mMAC(){return mMAC;}
    public byte[] getSessionKey() {return mSessionKey;}

    // Set Methods
    public void setLockId(int _lockId) {this.mLockId = _lockId;}

    public void setName(String _name) {
        this.mName = _name;
    }

    public void setMac(String _mac) {
        this.mMAC = _mac;
    }

    public void SetSessionKey (byte[] _SK) {
        this.mSessionKey = _SK;
    }

    @Override
    public String toString() {
        return this.mName + " ("+ this.mMAC+")";
    }
}
