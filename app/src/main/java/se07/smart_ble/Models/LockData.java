package se07.smart_ble.Models;

/**
 * Created by QuanLM on 10-Dec-16.
 */
public class LockData {

    private String mName;
    private String mMAC;
    private String mPin;

    public LockData(String _name, String _mac){
        this.mName = _name;
        this.mMAC = _mac;
        this.mPin = "";
    }

    public LockData () {
        this.mName = "";
        this.mMAC = "";
        this.mPin = "";
    }

    // Get Methods
    public String get_mName(){return mName;}
    public String get_mMAC(){return mMAC;}

    // Set Methods
    public void setName(String _name) {
        this.mName = _name;
    }

    public void setMac(String _mac) {
        this.mMAC = _mac;
    }
}
