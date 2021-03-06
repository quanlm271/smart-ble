package se07.smart_ble;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Serializable.SerializableListLockData;

public class LoginActivity extends AppCompatActivity {

    public Context _context = this;

    private EditText editText_email, editText_pwd;
    private Button button_login, button_register;
    private TextView textview_alertMessage;

    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;

    private JSONObject jsonData;
    private List<LockData> lsLockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Hide action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // EditText
        editText_email = (EditText) findViewById(R.id.editText_email);
        editText_pwd = (EditText)findViewById(R.id.editText_password);
        // TextView
        textview_alertMessage = (TextView)findViewById(R.id.textView_loginAlert);

        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();
        // List LockData object
        lsLockData = new ArrayList<LockData>();

        //Buttons
        button_login = (Button)findViewById(R.id.button_login);

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //exec task register
                new TaskRegister().execute(editText_email.getText().toString(), editText_pwd.getText().toString());
            }
        });

        button_register = (Button)findViewById(R.id.button_register);
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_context,RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    public class TaskRegister extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgresDialog = ProgressDialog.show(LoginActivity.this, "Please wait", "Login processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("email", params[0]);
            postParam.put("password", params[1]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/login", postParam);
                jsonData = new JSONObject(jsonString);
                return jsonData.getInt("result");
            }catch (Exception e) {
                e.printStackTrace();
                return Common.RESULT_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            m_ProgresDialog.dismiss();
            if(integer == Common.RESULT_SUCCESS) {
                Toast.makeText(LoginActivity.this, "Login success", Toast.LENGTH_LONG).show();
                Intent i = new Intent(_context,ListDeviceActivity.class);
                try {
                    // get list of lock data
                    JSONArray jsonArrayDevice = jsonData.getJSONArray("message");
                    for (int index = 0; index < jsonArrayDevice.length(); index++) {
                        JSONObject jsonDevice = jsonArrayDevice.getJSONObject(index);
                        LockData lockData = new LockData(jsonDevice.getString("name"), jsonDevice.getString("mac"));
                        lsLockData.add(lockData);
                    }
                    SerializableListLockData serializableListLockData = new SerializableListLockData(lsLockData);
                    i.putExtra("ListLockData", serializableListLockData);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else if(integer == Common.RESULT_USER_NOT_EXISTS) {
                Toast.makeText(LoginActivity.this, "User is not existed!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Login fail!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
