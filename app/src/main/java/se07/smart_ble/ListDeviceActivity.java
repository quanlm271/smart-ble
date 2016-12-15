package se07.smart_ble;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Serializable.SerializableListLockData;

public class ListDeviceActivity extends AppCompatActivity {

    public Context  _context = this;
    public String   _title = "List Device";

    private ListView listView_listDevice;
    private ArrayAdapter adapter_listDevice;

    // List LockData
    private List<LockData> listLockData;

    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device);

        setTitle(_title);

        //List View
        listView_listDevice = (ListView)findViewById(R.id.listView_listDevice);
        // List Lock Data
        listLockData = new ArrayList<LockData>();
        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();

        listView_listDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(_context, PinAccessActivity.class);
                startActivity(intent);
            }
        });

        //Adapter
        //String lock_01 = "LOCK 01 - MAC:A2-FC-79-6E-76-03";
        //String lock_02 = "LOCK 02 - MAC:80-E2-4C-5E-61-58";
        //String lock_03 = "LOCK 03 - MAC:B7-2A-E3-8B-8A-54";

        try {
            int userId = getIntent().getIntExtra("user_id", -1);
            //exec task register
            new TaskLoadDevices().execute(String.valueOf(userId));
        } catch (Exception e) {
            Log.v("Exception", e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.button_action_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.button_add_new_lock:
                Log.d(_title,"Add new action");
                Intent intent = new Intent(_context, AddDeviceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class TaskLoadDevices extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgresDialog = ProgressDialog.show(ListDeviceActivity.this, "Please wait", "Loading devices...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("uid", params[0]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/LoadDevice", postParam);
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
            m_ProgresDialog.dismiss();
            if(integer == Common.result_success) {
                Toast.makeText(ListDeviceActivity.this, "Loading success", Toast.LENGTH_LONG).show();
                try {
                    // get list devices
                    JSONArray jsonArrayDevice =  jsonData.getJSONArray("data");
                    for (int index = 0; index < jsonArrayDevice.length(); index++) {
                        JSONObject jsonDevice = jsonArrayDevice.getJSONObject(index);
                        LockData lockData = new LockData(jsonDevice.getString("name"), jsonDevice.getString("mac"));
                        listLockData.add(lockData);
                    }
                    // create list labels for listview
                    ArrayList<String> listLabel = new ArrayList<String>();
                    for (LockData lockData : listLockData) {
                        String label = lockData.get_mName() + " - " + lockData.get_mMAC();
                        listLabel.add(label);
                    }
                    // Adapter
                    adapter_listDevice = new ArrayAdapter<String>(_context, android.R.layout.simple_list_item_1, listLabel);
                    //Set data to listView
                    listView_listDevice.setAdapter(adapter_listDevice);
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else if(integer == Common.user_not_existing_code) {
                Toast.makeText(ListDeviceActivity.this, "Failed loading device!", Toast.LENGTH_LONG).show();
            }
        }
    }
}

