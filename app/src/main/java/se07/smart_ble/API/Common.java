package se07.smart_ble.API;

import android.widget.EditText;

import java.util.Arrays;

/**
 * Created by SangPham on 12/12/2016.
 */

public class Common {
    //static final String SERVICE_API_URL = "http://192.168.137.1:8081/register";
    public static final String SERVICE_API_URL = "http://10.0.2.2:8081";
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


}