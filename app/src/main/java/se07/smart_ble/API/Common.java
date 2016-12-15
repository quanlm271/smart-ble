package se07.smart_ble.API;

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
}