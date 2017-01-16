package se07.smart_ble;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.Serializable.SerializableListLockData;
import se07.smart_ble.Serializable.mySerializable;

public class ListDeviceActivity extends AppCompatActivity {

    public Context  _context = this;
    public String   _title = "List Device";
    private Intent intent;
    private static final String _TAG = "ListDeviceActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private bleLockService mService;
    public bleLockDevice mLock;
    private ListView listView_listDevice;
    private ArrayAdapter adapter_listDevice;

    private ArrayList<String> listOwnerDevice = new ArrayList<String>();

    // List LockData
    private ArrayList<LockData> listLockData;
    private Button btnAddNew;
    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;

    // Models
    private LockData lockData;
    private UserData userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_device);

        setTitle(_title);

        // get intent
        intent = this.getIntent();


        //Add 10-jan
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(ContextCompat.checkSelfPermission(_context,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)){
            }else{
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        bleDefine.PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }

        // Button AddNew
        btnAddNew = (Button) findViewById(R.id.btn_addNewLock);
        //List View
        listView_listDevice = (ListView)findViewById(R.id.listView_listDevice);
        // List Lock Data
        listLockData = new ArrayList<LockData>();
        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();

        // Initiate models
        userData = new UserData();
        lockData = new LockData();

        // Load Models
        Serializable serial = intent.getSerializableExtra(bleDefine.LOCK_DATA);
        if(serial != null) {
            mySerializable originMySerial = (mySerializable) serial;
            userData = originMySerial.getUserData();
        }

        // Trigger click event on button AddNew
        btnAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(mService != null) {
//                    mService.stopLeScanning();
////                    mService.disconnect(mLock);
//                }
                Intent intent = new Intent(_context, ListNewDeviceActivity.class);
                mySerializable desMySerial = new mySerializable();
                desMySerial.setUserData(userData);
                intent.putExtra(bleDefine.LOCK_DATA, desMySerial);
                startActivity(intent);
            }
        });

        // Inintiate service
        service_init();

        // Trigger click event on listview item
        listView_listDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                lockData = listLockData.get(position);
//
////                Intent i = new Intent(_context, PinAccessActivity.class);
////                i.putExtra("myserial", desMySerial);
////                startActivity(i);

//                Object obj = listView_listDevice.getItemAtPosition(position);
//                Log.w(_TAG,"Connect " + obj.toString());
//                bleLockDevice lock= (bleLockDevice) obj;
//                if(lock != null) {
//                    if (lock.ble_mac != lock.ble_mac)
//                        mService.disconnect(lock);
//                }
//                mLock = lock;
//                mService._connectToDevice(mLock);
////                bleLockDevice dLock = new LockData(mLock.ble_name, mLock.ble_mac,mLock.ble_sk);
//
//                for(LockData ld : listLockData){
//                    if(lock.ble_mac.equals(ld.get_mMAC().toUpperCase()))
//                        lockData = ld;
//                }

                Object obj = listView_listDevice.getItemAtPosition(position);
                Log.w(_TAG,"Connect " + obj.toString());
                lockData = (LockData) obj;
                if(lockData.IsInBound) {
                    // Get approriated bleDevice
                    mLock = mService.listDevice.get(lockData.get_mMAC());
                    mService._connectToDevice(mLock);
                } else {
                    // show toast here
                    Toast.makeText(ListDeviceActivity.this, "Device is not in bound!", Toast.LENGTH_LONG).show();
                }
                mySerializable desMySerial = new mySerializable(mLock);
                desMySerial.setUserData(userData);
                desMySerial.setLockData(lockData);
                Intent intent = new Intent(_context, PinAccessActivity.class);
                intent.putExtra(bleDefine.LOCK_DATA, desMySerial);
                startActivity(intent);
            }
        });

        //Adapter
        //String lock_01 = "LOCK 01 - MAC:A2-FC-79-6E-76-03";
        //String lock_02 = "LOCK 02 - MAC:80-E2-4C-5E-61-58";
        //String lock_03 = "LOCK 03 - MAC:B7-2A-E3-8B-8A-54";

        // Load list devices
        try {
            //int userId = getIntent().getIntExtra("user_id", -1);
            //exec task register
            new TaskLoadDevices().execute(String.valueOf(userData.getId()));
        } catch (Exception e) {
            Log.v("Exception", e.toString());
        }

        // Start Scanning BLE device to highligh device in bound
        //mService.StartBLE();
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
                Log.d(_title,"Scanning");
//                Intent intent = new Intent(_context, ListNewDeviceActivity.class);
//                startActivity(intent);
                if(mService != null)
                    mService.StartBLE();
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
                    // Display all lockdata from server

                    // get list devices
                    JSONArray jsonArrayDevice =  jsonData.getJSONArray("data");
                    for (int index = 0; index < jsonArrayDevice.length(); index++) {
                        JSONObject jsonDevice = jsonArrayDevice.getJSONObject(index);
                        LockData lockData = new LockData(jsonDevice.getInt("lock_id"), jsonDevice.getString("name"), jsonDevice.getString("mac"));
                        listLockData.add(lockData);
                    }
                    // create list labels for listview
//                    ArrayList<String> listLabel = new ArrayList<String>();
//                    for (LockData lockData : listLockData) {
//                        String label = lockData.get_mName() + " - " + lockData.get_mMAC();
//                        listOwnerDevice.add(label);
//                    }
                    //Adapter
                    adapter_listDevice = new ArrayAdapter<LockData>(_context, android.R.layout.simple_list_item_1, listLockData);
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
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder rawBinder)
        {
            mService = ((bleLockService.LocalBinder) rawBinder).getService();
            Log.d(_TAG, "onServiceConnected mService= " + mService);
            if (!mService.initialize()) {
                Log.e(_TAG, "Unable to initialize Bluetooth");
                finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService.disconnect(mLock);
            mService = null;

        }
    };

    private void service_init() {
        Log.d(_TAG, "INITIALIZING SERVICE");
        Intent bindIntent = new Intent(this, bleLockService.class);
        bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver(LockStatusChangeReceiver, nokeUpdateIntentFilter());
    }

    private final BroadcastReceiver LockStatusChangeReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(bleDefine.LOCK_FOUND)) {
                Log.d(_TAG, "FOUND" + mService.listDevice.size());
                //ArrayList<bleLockDevice> _list = new ArrayList<bleLockDevice>();
                for (bleLockDevice d : mService.listDevice.values()) {
                    Log.w(_TAG, "MAC: " + d.ble_mac + " Name: " + d.ble_name);
                    for(LockData ld: listLockData)
                    {
                        if(d.ble_mac.equals(ld.get_mMAC().toUpperCase())) {
                            //_list.add(d);
                            ld.IsInBound = true;
                        } else {
                            ld.IsInBound = false;
                        }
                    }
                    //_list.add(d);
                }
                //adapter_listDevice = new ArrayAdapter(_context, android.R.layout.simple_list_item_1, _list);
                //listView_listDevice.setAdapter(adapter_listDevice);
                for (int i = 0; i < listLockData.size(); i++) {
                    if(listLockData.get(i).IsInBound) {
                        listView_listDevice.getChildAt(i).setBackgroundColor(Color.parseColor("#b3e784"));
                    }
                }
            }
            if (action.equals(bleDefine.BLE_CONNECTED)) {
                Log.d("AAd", "DDD");

                if (action.equals(bleDefine.RECEIVED_SERVER_DATA)) {
                    Log.w(_TAG, "RECEIVED_SERVER_DATARECEIVED_SERVER_DATA");

                    String mac = intent.getStringExtra(bleDefine.MAC_ADDRESS);
                    bleLockDevice newLock = mService.listDevice.get(mac);

                    byte[] data = intent.getByteArrayExtra(bleDefine.EXTRA_DATA);
                    switch (data.length) {
                        case 6:
                            newLock.ble_sk = bleDefine.bytesToHex(data);
                            break;
                        default:
                            break;
                    }
                    Log.w(_TAG, "DATA RECEIVED: " + bleDefine.bytesToHex(data));
                }
            }
        }
    };
    private IntentFilter nokeUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(bleDefine.LOCK_FOUND);
        intentFilter.addAction(bleDefine.BLE_CONNECTED);
        intentFilter.addAction(bleDefine.BLE_DISCONNECTED);
        intentFilter.addAction(bleDefine.BLE_CONNECTING);
        intentFilter.addAction(bleDefine.INVALID_NOKE_DEVICE);
        intentFilter.addAction(bleDefine.BLUETOOTH_GATT_ERROR);
        intentFilter.addAction(bleDefine.RECEIVED_SERVER_DATA);
        intentFilter.addAction(bleDefine.RECEIVED_APP_DATA);
        return intentFilter;
    }
}

