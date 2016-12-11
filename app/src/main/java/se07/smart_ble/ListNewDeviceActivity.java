package se07.smart_ble;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListNewDeviceActivity extends AppCompatActivity {

    private Context _context = this;
    private static  final String _TITLE = "ADD LOCK";
    private static final String _TAG = "ListNewDeviceActivity";

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 10000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;

    private Button btnWrite, btnScan;

    private final String ms_key = "6756386345397A31344D387036324531";
    private String m_data= "6D696E685175616E0000000000000000";

    private bleLockService mService;
    private ArrayAdapter arrayAdapter;
    public bleLockDevice mLock;

    private mySerializable mSerialeizable;

    private ListView listView_newDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_new_device);

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
        mSerialeizable = new mySerializable(null);

        listView_newDevice = (ListView) findViewById(R.id.listView_newDevice);
        listView_newDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = listView_newDevice.getItemAtPosition(position);
                Log.w(_TAG,"Connect " + obj.toString());
                bleLockDevice lock= (bleLockDevice) obj;
                if(lock != null) {
                    if (lock.ble_mac != lock.ble_mac)
                        mService.disconnect(lock);
                }
                mLock = lock;
                mService._connectToDevice(mLock);
                LockData dLock = new LockData(mLock.ble_name, mLock.ble_mac);
                Intent intent = new Intent(_context, AddDeviceActivity.class);
                intent.putExtra("mData",new mySerializable(dLock));
                startActivity(intent);
            }
        });
        service_init();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case bleDefine.PERMISSION_REQUEST_COARSE_LOCATION: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(_TAG, "coarse location permission granted");
                } else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                        }

                    });
                    builder.show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mService != null)
            mService.StartBLE();
//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        } else {
//            if (Build.VERSION.SDK_INT >= 21) {
//                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
//                settings = new ScanSettings.Builder()
//                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                        .build();
//                filters = new ArrayList<ScanFilter>();
//            }
//            scanLeDevice(true);
//        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
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
            if(action.equals(bleDefine.LOCK_FOUND)){
                Log.d(_TAG,"FOUND"+mService.listDevice.size());
                ArrayList<bleLockDevice> _list = new ArrayList<bleLockDevice>();
                for (bleLockDevice d: mService.listDevice.values())
                {
                    Log.w(_TAG,"MAC: "+d.ble_mac+" Name: "+d.ble_name);
                    _list.add(d);
                }
                arrayAdapter = new ArrayAdapter(_context,android.R.layout.simple_list_item_1,_list);
                listView_newDevice.setAdapter(arrayAdapter);
            }
            if(action.equals(bleDefine.BLE_CONNECTED)){
                mLock.sendCommand("00A0");
                AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                builder.setTitle("Command");

//                View viewInflated = LayoutInflater.from(_context).inflate(R.layout.action_dialog, null);
//                // Set up the input
//                final Button btnSendData = (Button) viewInflated.findViewById(R.id.btnSendData);
//                final Button btnCreateMSkey = (Button) viewInflated.findViewById(R.id.btnCreateMSkey);
//                final Button btnMSkey = (Button) viewInflated.findViewById(R.id.btnMSkey);
//                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//                btnSendData.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        //mLock.sendCommand(bleDefine.CMD_CONNECT);
//                        byte[] b_m_key = bleDefine.hexToBytes(ms_key);
//                        byte[] b_m_data = bleDefine.hexToBytes(m_data);
//                        aes.aes_enc_dec(b_m_data,b_m_key,(byte)0);
//                        Log.d(_TAG,"ENC: "+bleDefine.bytesToHex(b_m_data));
//                        mLock.sendCommand(bleDefine.bytesToHex(b_m_data));
//
//                    }
//                });
//                btnCreateMSkey.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mLock.sendCommand("00A3");
//                    }
//                });
//                //Demo Get MS key
//                btnMSkey.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mLock.sendCommand("00A1");
//                    }
//                });
//                builder.setView(viewInflated);
//
////                // Set up the buttons
////                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
////                    @Override
////                    public void onClick(DialogInterface dialog, int which) {
////                        dialog.dismiss();
////                        m_Text = input.getText().toString();
////                        String quickClickCode = m_Text;
////                        Log.d(_TAG, "Quick click code " + quickClickCode);
////
////                        m_Text = usecount.getText().toString();
////                        int useCount = Integer.parseInt(m_Text);
////                        Log.d(_TAG, "Use count: " + useCount);
////                        int commandid = 0; //CHANGE THIS TO USE THE COMMAND QUEUE
////                        m_noke.sendCommand(createSetQC(useCount, quickClickCode));
////
////                    }
////                });
////                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
////                    @Override
////                    public void onClick(DialogInterface dialog, int which) {
////                        dialog.cancel();
////                    }
////                });
//
//                builder.show();
            }
            //*********************//
        }
    };
    private static IntentFilter nokeUpdateIntentFilter() {
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
