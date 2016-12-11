package se07.smart_ble;

/**
 * Created by QuanLM on 10-Dec-16.
 */
public class LockData {

    private String mName;
    private String mMAC;

    public LockData(String name, String mac){
        this.mName = name;
        this.mMAC = mac;
    }

    public String get_mName(){return mName;}
    public String get_mMAC(){return mMAC;}
}
