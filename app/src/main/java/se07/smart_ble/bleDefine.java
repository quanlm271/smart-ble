package se07.smart_ble;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Created by QuanLM on 06-Dec-16.
 */public class bleDefine {

    public static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID RX_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    public static final UUID STATE_CHAR_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    //Command Define
    public static final byte CMD_CONNECT        = (byte)0xA0;
    public static final byte CMD_UNLOCK         = (byte)0xA1;
    public static final byte CMD_LOCK           = (byte)0xA2;
    public static final byte CMD_CHANGEPASS     = (byte)0xA3;
    public static final byte CMD_RESET          = (byte)0xA4;

    //Bluetooth States
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int GATT_ERROR = 133;

    //Bluetooth Gatt Callbacks
    public static final String ACTION_GATT_CONNECTED               = "ACTION_GATT_CONNECTED";
    public static final String ACTION_GATT_DISCONNECTED            = "ACTION_GATT_DISCONNECTED";
    public static final String ACTION_GATT_SERVICES_DISCOVERED     = "ACTION_GATT_SERVICES_DISCOVERED";
    public static final String ACTION_DATA_AVAILABLE               = "ACTION_DATA_AVAILABLE";
    public static final String EXTRA_DATA                          = "EXTRA_DATA";
    public static final String MAC_ADDRESS                         = "MAC_ADDRESS";
    public static final String INVALID_NOKE_DEVICE                 = "INVALID_NOKE_DEVICE";
    public static final String TX_NOTIFICATION_ENABLED             = "TX_NOTIFICATION_ENABLED";
    public static final String STATE_CHARACTERISTIC_READ           = "STATE_CHARACTERISTIC_READ";
    public static final String BLUETOOTH_GATT_ERROR                = "BLUETOOTH_GATT_ERROR";

    //BLE Broadcasts
    public static final String BLE_CONNECTING                     ="BLE_CONNECTING";
    public static final String BLE_CONNECTED                      ="BLE_CONNECTED";
    public static final String BLE_DISCONNECTED                   ="BLE_DISCONNECTED";
    public static final String RECEIVED_SERVER_DATA                ="RECEIVED_SERVER_DATA";
    public static final String RECEIVED_APP_DATA                   ="RECEIVED_APP_DATA";

    public static final String LOCK_FOUND                          ="LOCK_FOUND";

    public static String bytesToHex(byte[] bytes) {
        if(bytes != null) {
            char[] hexChars = new char[bytes.length * 2];
            for (int j = 0; j < bytes.length; j++) {
                int v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
        else
        {
            return "";
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static byte[] hexToBytes(String hexstring) {
        hexstring = hexstring.toUpperCase();
        int len = hexstring.length() / 2;
        int hexstringlen = len * 2;
        byte[] bytes = new byte[len];
        for (int x = 0; x < len; x++) {
            for (int y = 0; y < hexArray.length; y++) {
                if (hexArray[y] == hexstring.charAt(2 * x)) {
//                    bytes[len-1-x]+=(byte)(y<<4);
                    bytes[x] += (byte) (y << 4);
                }
                if (hexArray[y] == hexstring.charAt(2 * x + 1)) {
//                    bytes[len-1-x]+=(byte)y;
                    bytes[x] += (byte) y;
                }
            }
        }
        return bytes;
    }

    public static byte[] MacToBytes(String Mac){
        String strMac = Mac.replace(":","");
        byte[] byteMac = bleDefine.hexToBytes(strMac);
        return byteMac;
    }

}

