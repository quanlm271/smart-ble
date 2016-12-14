package se07.smart_ble;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by QuanLM on 06-Dec-16.
 */

public class bleLockService extends Service {

    private final static String TAG = bleLockService.class.getSimpleName();

    //List lock
    public Map<String, bleLockDevice> listDevice;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothScanner;
    private ScanCallback mNewBluetoothScanCallback;
    private BluetoothAdapter.LeScanCallback mOldBluetoothScanCallback;
    private boolean mScanning;

    public class LocalBinder extends Binder {
        public bleLockService getService() {
            return bleLockService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listDevice = new HashMap<>();
    }

    public void addDevice(bleLockDevice mLock){
        mLock.mService = this;
        mLock.mContext = getApplicationContext();
        listDevice.put(mLock.ble_mac, mLock);
    }

    public boolean initialize(){
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public void stopLeScanning(){
        Log.w(TAG, "STOP SCANNING");
        mScanning = false;
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                mBluetoothScanner.stopScan(mNewBluetoothScanCallback);

            } else
            {
                mBluetoothAdapter.stopLeScan(mOldBluetoothScanCallback);
            }
        }
    }

    public void StartBLE(){
        if (mBluetoothAdapter !=null){
            startLeScanning();
        }
        else
        {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();

            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
            {
                //TODO Handle cases when bluetooth is turned off
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            } else {
                startLeScanning();
            }
        }
    }

    public void startLeScanning(){
        Log.w(TAG, "START SCANNING");
        mScanning = true;
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                initNewBluetoothCallback();

                mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
                List<ScanFilter> scanFilters = new ArrayList<>();
                ScanSettings settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                        .build();
                mBluetoothScanner.startScan(scanFilters, settings, mNewBluetoothScanCallback);
            }
            else
            {
                initOldBluetoothCallback();
                mBluetoothAdapter.startLeScan(mOldBluetoothScanCallback);
            }
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initNewBluetoothCallback(){
        mNewBluetoothScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);

                if(result.getDevice() != null){
                    final BluetoothDevice bt_device = result.getDevice();
                    final ScanRecord broadcastData = result.getScanRecord();
                    final int rssi = result.getRssi();
                    if(isbleLockDevice(broadcastData))
                    {

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if(bt_device != null){
                                    //Log.d("SERVICE", "BROADCAST DATA: " + bleDefine.bytesToHex(broadcastData.getManufacturerSpecificData().valueAt(0)));
                                    Log.i(TAG, "Connected to GATT 121211server.");
                                    String intentAction = bleDefine.LOCK_FOUND;
                                    //noke.connectionState = bleDefine.STATE_DISCONNECTED;
                                    Log.i(TAG, "Disconnected from GATT server.");
                                    broadcastUpdate(intentAction);
                                    connectToDevice(bt_device, rssi);
                                }
                            }
                        });

                    }

                }
            }
        };
    }

    private void initOldBluetoothCallback(){
        mOldBluetoothScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice bluetoothDevice, int i, byte[] bytes) {

            }
        };
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean isbleLockDevice(ScanRecord broadcastData){

        String name = broadcastData.getDeviceName();
        if(name.contains("HM"))
            return true;
        return false;
    }

    public void _connectToDevice(bleLockDevice mLock){
        stopLeScanning();
        if (mLock.connectionState != bleDefine.STATE_CONNECTING) {

            mBluetoothAdapter.cancelDiscovery();
            //TODO check RSSI levels before connecting

            if (mLock.gatt == null) {
                connectToGatt(mLock);
            } else {
                mLock.gatt.disconnect();
                mLock.gatt.close();
                mLock.gatt = null;

                startLeScanning();
            }
        }
    }

    private void connectToDevice(BluetoothDevice bt_device, int rssi){

        bleLockDevice mLock  = listDevice.get(bt_device.getAddress());

        if(mLock != null){
            stopLeScanning();
            Log.w(TAG, "Found mLock Device: " + mLock.ble_name + " Connecting State: " + mLock.connectionState + " Gatt: " + mLock.gatt);

            if (mLock.bluetoothDevice == null) {
                mLock.bluetoothDevice = bt_device;
            }

//            if (mLock.connectionState != bleDefine.STATE_CONNECTING) {
//
//                mBluetoothAdapter.cancelDiscovery();
//                //TODO check RSSI levels before connecting
//
//                if (mLock.gatt == null) {
//                    connectToGatt(mLock);
//                } else {
//                    mLock.gatt.disconnect();
//                    mLock.gatt.close();
//                    mLock.gatt = null;
//
//                    startLeScanning();
//                }
//            }
        }
        else
        {
            stopLeScanning();
            mLock = new bleLockDevice(bt_device.getName(), bt_device.getAddress());
            addDevice(mLock);
            Log.w(TAG, "Found New mLock Device: " + mLock.ble_name + " Connecting State: " + mLock.connectionState + " Gatt: " + mLock.gatt);
            if (mLock.bluetoothDevice == null) {
                mLock.bluetoothDevice = bt_device;
            }
//            if (mLock.connectionState != bleDefine.STATE_CONNECTING) {
//                mBluetoothAdapter.cancelDiscovery();
//
//                //TODO check RSSI levels before connecting
//
//                if (mLock.gatt == null) {
//
//                    connectToGatt(mLock);
//
//                } else {
//                    mLock.gatt.disconnect();
//                    mLock.gatt.close();
//                    mLock.gatt = null;
//                }
//            }

        }
    }

    public boolean connectToGatt(bleLockDevice mLock){
        if (mBluetoothAdapter == null || mLock == null) {
            Log.e(TAG, "BluetoothAdapter not initialized or unspecified device.");
            return false;
        }

        if(mLock.bluetoothDevice == null)
        {
            Log.e(TAG, "Device not found. Unable to connect.");
            return false;
        }

        mLock.gatt = mLock.bluetoothDevice.connectGatt(this, false, mGattCallback);
        return true;
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.w(TAG, "On connection state changed. Status: " + status + " New State: " + newState);

            bleLockDevice noke = listDevice.get(gatt.getDevice().getAddress());
            String intentAction;

            if(status == bleDefine.GATT_ERROR) {

                noke.gatt.disconnect();
                noke.gatt.close();
                noke.gatt = null;

                intentAction = bleDefine.BLUETOOTH_GATT_ERROR;
                broadcastUpdate(intentAction);

                noke.connectionState = bleDefine.STATE_DISCONNECTED;

                Log.e(TAG, "133 ERROR, EXITING BLUETOOTH GATT CALLBACK");

                return;
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = bleDefine.BLE_CONNECTING;
                noke.connectionState = bleDefine.STATE_CONNECTED;
                broadcastUpdate(intentAction, gatt.getDevice().getAddress());
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.

                Log.i(TAG, "Attempting to start service discovery:" +
                        noke.gatt.discoverServices());


            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = bleDefine.BLE_DISCONNECTED;
                noke.connectionState = bleDefine.STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            bleLockDevice noke = listDevice.get(gatt.getDevice().getAddress());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + noke.gatt);
                broadcastUpdate(bleDefine.ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress());
                readStateCharacteristic(noke);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            Log.w(TAG, "CHARACTERISTIC HAS BEEN READ: " + bleDefine.bytesToHex(characteristic.getValue()) + " UUID: " + characteristic.getUuid());

            if (status == BluetoothGatt.GATT_SUCCESS)
            {
                if(bleDefine.STATE_CHAR_UUID.equals(characteristic.getUuid()))
                {
                    bleLockDevice noke = listDevice.get(gatt.getDevice().getAddress());
                    //noke.SetStatus(characteristic.getValue());
                    enableTXNotification(noke);
                }
                else
                {
                    broadcastUpdate(bleDefine.ACTION_DATA_AVAILABLE, characteristic);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.w(TAG, "On Characteristic Changed: " + bleDefine.bytesToHex(characteristic.getValue()));

            bleLockDevice mLock = listDevice.get(gatt.getDevice().getAddress());
            byte[] data=characteristic.getValue();

            Log.w(TAG, mLock.ble_mac);
            //byte[] resultdata = new byte[20];
            //bleDefine.copyArray(resultdata,0,data,0,20);
            //Log.w(TAG,bleDefine.bytesToHex(resultdata));
            broadcastUpdate(bleDefine.RECEIVED_SERVER_DATA, data, mLock.ble_mac);

//
//            byte destination = data[0];
//            if(destination == bleDefine.SERVER_Dest)
//            {
//                //nokeServerSDK.RxDataFromLock(result);
//
//
//            }
//            else if(destination==bleDefine.APP_Dest)
//            {
//                if(noke.TxToLockPackets.size() > 0) {
//                    noke.TxToLockPackets.remove(0);
//
//                    Log.d("DEVICE", "PACKETS LEFT: " + noke.TxToLockPackets.size());
//
//                    if (noke.TxToLockPackets.size() > 0) {
//
//                        writeRXCharacteristic(noke);
//                    }
//                }
//
//                byte resulttype = data[1];
//
//                broadcastUpdate(bleDefine.RECEIVED_APP_DATA, data, noke.d_mac);
//
//                if(resulttype == bleDefine.SHUTDOWN_ResultType)
//                {
//                    Log.w(TAG, "THE LOCK LOCKED ON CHARACTERISTIC");
//                    disconnect(noke);
//                }
//            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.w(TAG, "On Descriptor Write: " + descriptor.toString() + " Status: " + status);
            broadcastUpdate(bleDefine.BLE_CONNECTED, gatt.getDevice().getAddress());
        }
    };

    public void enableTXNotification(bleLockDevice noke)
    {

        if (noke.gatt == null) {
            showMessage("Gatt is null");
            broadcastUpdate(bleDefine.INVALID_NOKE_DEVICE);
            return;
        }

        BluetoothGattService RxService = noke.gatt.getService(bleDefine.RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("Rx service not found!");
            broadcastUpdate(bleDefine.INVALID_NOKE_DEVICE);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(bleDefine.TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            broadcastUpdate(bleDefine.INVALID_NOKE_DEVICE);
            return;
        }
        noke.gatt.setCharacteristicNotification(TxChar, true);

        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(bleDefine.CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        noke.gatt.writeDescriptor(descriptor);
    }

    public void readStateCharacteristic(bleLockDevice noke){
        if (mBluetoothAdapter == null || noke.gatt == null){
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        BluetoothGattService RxService = noke.gatt.getService(bleDefine.RX_SERVICE_UUID);

        if(noke.gatt == null) {
            showMessage("mBluetoothGatt null " + noke.gatt);
        }

        if (RxService == null){
            showMessage("Rx service not found!");
            broadcastUpdate(bleDefine.INVALID_NOKE_DEVICE);
            return;
        }
        BluetoothGattCharacteristic StateChar = RxService.getCharacteristic(bleDefine.STATE_CHAR_UUID);
        if (StateChar == null) {
            showMessage("State characteristic not found!");
            broadcastUpdate(bleDefine.INVALID_NOKE_DEVICE);
            return;
        }
        noke.gatt.readCharacteristic(StateChar);
    }

    public boolean writeRXCharacteristic (bleLockDevice mLock){
        if (mLock.gatt == null) {
            Log.e(TAG, "lost connection");
            return false;
        }
        BluetoothGattService Service = mLock.gatt.getService(bleDefine.RX_SERVICE_UUID);
        if (Service == null) {
            Log.e(TAG, "service not found!");
            return false;
        }
        BluetoothGattCharacteristic charac = Service
                .getCharacteristic(bleDefine.STATE_CHAR_UUID);
        if (charac == null) {
            Log.e(TAG, "char not found!");
            return false;
        }
        byte[] data = mLock.TxDataToLock.get(0).data;
        charac.setValue(data);
        boolean status = mLock.gatt.writeCharacteristic(charac);
        return status;

    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is handling for the notification on TX Character of Noke service
        if (bleDefine.TX_CHAR_UUID.equals(characteristic.getUuid()))
        {
            intent.putExtra(bleDefine.EXTRA_DATA, characteristic.getValue());
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, final String address)
    {
        final Intent intent = new Intent(action);

        intent.putExtra(bleDefine.MAC_ADDRESS, address);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    private void broadcastUpdate(final String action, byte[] data, final String address)
    {
        final Intent intent = new Intent(action);
        intent.putExtra(bleDefine.EXTRA_DATA, data);
        intent.putExtra(bleDefine.MAC_ADDRESS, address);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }

    public void disconnect(final bleLockDevice noke){

        if (mBluetoothAdapter == null || noke.gatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                noke.gatt.disconnect();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                noke.gatt.close();
                noke.gatt = null;

            }
        });

    }


}
