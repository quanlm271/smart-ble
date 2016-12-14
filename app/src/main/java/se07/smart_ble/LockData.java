package se07.smart_ble;

/**
 * Created by QuanLM on 10-Dec-16.
 */
public class LockData {

    private String mName;
    private String mMAC;
    private String mSK;

    public LockData(String _name, String _mac, String _sk){
        this.mName = _name;
        this.mMAC = _mac;
        this.mSK = _sk;
    }

    public String get_mName(){return mName;}
    public String get_mMAC(){return mMAC;}
    public String get_mSk(){return mSK;}
}
