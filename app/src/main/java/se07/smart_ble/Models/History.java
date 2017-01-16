package se07.smart_ble.Models;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;

/**
 * Created by SangPham on 1/16/2017.
 */

public class History {
    private int mId;
    private int mUserId;
    private String mUserName;
    private int mLockId;
    private String mCommand;
    private String mTimeStamp;
    private String mLocation;

    // AsynTask
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;

    // Contructor for loading from Server
    public History() {
        this.mId = -1;
        this.mUserId = -1;
        this.mUserName = "";
        this.mLockId = -1;
        this.mCommand = "";
        this.mTimeStamp = "";
        this.mLocation = "";
    }

    // Contructor for saving to server
    public History(Context context, int _userId, int _lockId, String _command) {
        this.mId = -1;
        this.mUserId = _userId;
        this.mUserName = "";
        this.mLockId = _lockId;
        this.mCommand = _command;
        this.mTimeStamp = Common.GetCurrentDatetime(Common.datetime_format);
        this.mLocation = Common.GetCurrentLocation(context);
        m_AccessServiceAPI = new AccessServiceAPI();
        jsonData = new JSONObject();
    }

    public int getId () {return mId;}
    public int getUserId () {return mUserId;}
    public String getUserName () {return mUserName;}
    public int getLockId () {return mLockId;}
    public String getCommand () {return mCommand;}
    public String getTimeStamp () {return mTimeStamp;}
    public String getLocation () {return mLocation;}

    public void SetId(int _id) {this.mId = _id;}
    public void SetUserId(int _uId) {this.mUserId = _uId;}
    public void SetUserName(String _userName) {this.mUserName = _userName;}
    public void SetLockId(int _lockId) {this.mLockId = _lockId;}
    public void SetCommand(String _command) {this.mCommand = _command;}
    public void SetTimeStamp(String _timeStamp) {this.mTimeStamp = _timeStamp;}
    public void SetLocation(String _location) {this.mLocation = _location;}

    @Override
    public String toString() {
        return "User " + this.getUserName() + " called " + this.mCommand + " command at " + this.mTimeStamp + " in " + this.mLocation;
    }

    public void SaveHistory() {
        try {
            new TaskSaveHistory().execute();
        } catch (Exception e) {
            Log.v("Save History", e.toString());
        }
    }

    public class TaskSaveHistory extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("user_id", Integer.toString(mUserId));
            postParam.put("lock_id", Integer.toString(mLockId));
            postParam.put("command", mCommand);
            postParam.put("timestamp", mTimeStamp);
            postParam.put("location", mLocation);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/SaveHistory", postParam);
                jsonData = new JSONObject(jsonString);
                return jsonData.getInt("result");
            }catch (Exception e) {
                e.printStackTrace();
                return Common.exception_code;
            }
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == Common.result_success) {
                Log.v("Save History", "Saved History successfully");
            } else {
                Log.v("Save History", "Failed to save history");
            }
        }
    }
}
