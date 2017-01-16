package se07.smart_ble.API;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;

/**
 * Created by SangPham on 12/12/2016.
 */

public class Common {
    public static final String SERVICE_API_URL = "http://192.168.137.250:8081";
    //public static final String SERVICE_API_URL = "http://10.0.2.2:8081";
//    public static final String SERVICE_API_URL = "http://10.45.7.213:8081";
    // public static final String SERVICE_API_URL = "http://10.45.130.235:8081";
    public static final int exception_code = 0;
    public static final int database_query_failed_code = 1;
    public static final int incorrect_requested_format_json_code = 2;
    public static final int user_existing_code = 3;
    public static final int user_not_existing_code = 4;
    public static final int user_register_success_code = 5;
    public static final int login_success_code = 6;
    public static final int user_not_owns_lock = 7;
    public static final int user_owns_lock = 8;
    public static final int lock_not_exist = 9;
    public static final int result_success = 10;
    public static final int lock_has_owner_code = 11;
    public static final int lock_has_no_owner_code = 12;
    public static final int pin_not_correct_code = 13;
    public static final int pin_does_not_match = 14;

    public static final String datetime_format = "MM/dd/yyyy HH:mm:ss";

    public static boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }

    // pin: "1234"
    public static String PinToHex (String pin) {
        String hex = "";
        String[] pinArray = pin.split("");
        for(int i = 1; i < pinArray.length; i++) {
            int n = Integer.parseInt(pinArray[i]);
            hex += "0" + Integer.toHexString(n);
        }
        return hex;
    }

    // Tạo data user mẫu, set các trường phù hợp với dữ liệu muốn tạo.
    public static UserData GenerateUserData() {
        UserData userData = new UserData();
        userData.setEmail("feeder@email");
        userData.setName("MasterYi");
        userData.setPwd("Pentakill");
        return userData;
    }

    // Tạo lock data mẫu.
    public static LockData GenerateLockData() {
        LockData lockData = new LockData();
        lockData.setMac("ec:1a:59:61:07:b2");
        lockData.setName("LOCK 01");

        return lockData;
    }

    // Generate Key AES
    public static byte[] GenerateKeyAES (int keySize) {
        try {
            KeyGenerator gen = KeyGenerator.getInstance("AES");
            gen.init(keySize);
            SecretKey k = gen.generateKey();
            return k.getEncoded();
        } catch (Exception e) {
            Log.d("GenerateKeyAES Error", e.toString());
            return null;
        }
    }

    public static String GetCurrentLocation (Context context) {
        Geocoder gCoder = new Geocoder(context, Locale.ENGLISH);
        GPSTracker gpsTracker = new GPSTracker(context);
        try {
            List<Address> addresses = gCoder.getFromLocation(gpsTracker.latitude, gpsTracker.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0).getSubAdminArea() + ", " + addresses.get(0).getAdminArea();
            }
            return "";
        } catch (Exception e) {
            Log.v("Get Location", e.toString());
            return "";
        }
    }

    public static String GetCurrentDatetime(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }
}