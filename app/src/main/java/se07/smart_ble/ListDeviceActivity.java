package se07.smart_ble;

import android.content.Context;
import android.content.Intent;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import se07.smart_ble.Serializable.SerializableListLockData;

public class ListDeviceActivity extends AppCompatActivity {

    public Context  _context = this;
    public String   _title = "List Device";

    private ListView listView_listDevice;
    private ArrayAdapter adapter_listDevice;

    // List LockData
    private List<LockData> listLockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device);

        setTitle(_title);

        //List View
        listView_listDevice = (ListView)findViewById(R.id.listView_listDevice);
        // List Lock Data
        listLockData = new ArrayList<LockData>();

        //Adapter
        //String lock_01 = "LOCK 01 - MAC:A2-FC-79-6E-76-03";
        //String lock_02 = "LOCK 02 - MAC:80-E2-4C-5E-61-58";
        //String lock_03 = "LOCK 03 - MAC:B7-2A-E3-8B-8A-54";

        try {
            SerializableListLockData serializableListLockData = (SerializableListLockData) getIntent().getSerializableExtra("ListLockData");
            List<LockData> listLockData = serializableListLockData.getListLockData();
            ArrayList<String> listLabel = new ArrayList<String>();
            for (LockData lockData : listLockData) {
                String label = lockData.get_mName() + " - " + lockData.get_mMAC();
                listLabel.add(label);
            }
            // Adapter
            adapter_listDevice = new ArrayAdapter<String>(_context, android.R.layout.simple_list_item_1, listLabel);
            //Set data to listView
            listView_listDevice.setAdapter(adapter_listDevice);
            listView_listDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(_context, PinAccessActivity.class);
                    startActivity(intent);
                }
            });
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
                Intent intent = new Intent(_context, ListNewDeviceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }
}

