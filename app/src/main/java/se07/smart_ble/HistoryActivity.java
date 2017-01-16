package se07.smart_ble;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Models.History;
import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.Serializable.mySerializable;

public class HistoryActivity extends AppCompatActivity {

    private Context _context = this;
    private static final String _TITLE = "History";

    // Views
    private ListView listview_history;

    // Models
    private bleLockDevice bleDevice;
    private UserData userData;
    private LockData lockData;
    private ArrayList<History> listHistory;

    // AsynTask
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;
    private JSONArray jsonArrayHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        setTitle(_TITLE);

        // Initiate Views
        listview_history = (ListView) findViewById(R.id.listView_history);

        // Initiate models
        //bleDevice = new bleLockDevice();
        //userData = new UserData();
        //lockData = new LockData();

        // Load Models
        Serializable serial = getIntent().getSerializableExtra(bleDefine.LOCK_DATA);
        if(serial != null) {
            mySerializable originalMySerial = (mySerializable) serial;
            bleDevice = originalMySerial.getLOCK();
            userData = originalMySerial.getUserData();
            lockData = originalMySerial.getLockData();
        }

        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();
        jsonArrayHistory = new JSONArray();
        // list history object
        listHistory = new ArrayList<History>();

        // Load View
        // Load listview history
        new TaskLoadHistory().execute(Integer.toString(lockData.getLockId()));
    }

    public class TaskLoadHistory extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("lock_id", params[0]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/LoadHistory", postParam);
                jsonData = new JSONObject(jsonString);
                return jsonData.getInt("result");
            }catch (Exception e) {
                Log.d("Load History", e.toString());
                return Common.exception_code;
            }
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if(integer == Common.result_success) {
                try {
                    // get list owners
                    jsonArrayHistory = jsonData.getJSONArray("data");
                    for (int index = 0; index < jsonArrayHistory.length(); index++) {
                        JSONObject jsonHis = jsonArrayHistory.getJSONObject(index);
                        History his = new History();
                        his.SetId(jsonHis.getInt("id"));
                        his.SetUserId(jsonHis.getInt("user_id"));
                        his.SetUserName(jsonHis.getString("user_name"));
                        his.SetLockId(jsonHis.getInt("lock_id"));
                        his.SetCommand(jsonHis.getString("command"));
                        his.SetTimeStamp(jsonHis.getString("timestamp"));
                        his.SetLocation(jsonHis.getString("location"));
                        listHistory.add(his);
                    }
                    ArrayAdapter<History> arrayAdapter =
                            new ArrayAdapter<History>(
                                    _context,
                                    android.R.layout.simple_list_item_1,
                                    listHistory);
                    listview_history.setAdapter(arrayAdapter);
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(_context, "Failed to connect server!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
