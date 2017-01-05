package se07.smart_ble.Models;

import java.util.ArrayList;

/**
 * Created by SangPham on 12/16/2016.
 */

public class UserData {
    private int mId;
    private String mName, mEmail, mPwd;
    private ArrayList<LockData> mListLock;

    public UserData () {
        mId = -1;
        mName = "";
        mEmail = "";
        mPwd = "";
        mListLock = new ArrayList<LockData>();
    }

    // Set properties
    public void setId(int id) {
        this.mId = id;
    }
    public void setName(String name) {
        this.mName = name;
    }
    public void setEmail(String email) {
        this.mEmail = email;
    }
    public void setPwd(String pwd) {
        this.mPwd = pwd;
    }
    public void setListLock(ArrayList<LockData> listLock) {
        this.mListLock = listLock;
    }

    // Get properties
    public int getId () {
        return mId;
    }
    public String getName() {
        return mName;
    }
    public String getEmail() {
        return mEmail;
    }
    public String getPwd() {
        return mPwd;
    }
    public ArrayList<LockData> getListLock() {
        return mListLock;
    }
}
