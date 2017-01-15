package se07.smart_ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by QuanLM on 06-Dec-16.
 */
public class bleLockDevice {

    public String ble_mac;
    public String ble_name;
    public String ble_sk;

    public int connectionState;

    public Context mContext;
    public bleLockService mService;

    public BluetoothGatt gatt;
    public BluetoothDevice bluetoothDevice;

    public ArrayList<blePacket> TxDataToLock = new ArrayList<blePacket>();
    public ArrayList<blePacket> TxDataToServer;

    public bleLockDevice(String name, String address) {
        ble_name = name;
        ble_mac = address;
    }

    public bleLockDevice() {
        ble_name = "";
        ble_mac = "";
        ble_sk = "";
    }

    @Override
    public String toString() {
        return this.ble_name + " ("+ this.ble_mac+")";
    }

    public void sendCommand(String command) {
        Log.d("AA", "AA");
        this.TxDataToLock.clear();
        blePacket p = new blePacket();
        p.data = bleDefine.hexToBytes(command);
        this.TxDataToLock.add(p);
        mService.writeRXCharacteristic(this);
    }
}
