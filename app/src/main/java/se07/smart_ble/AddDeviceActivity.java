package se07.smart_ble;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.Serializable.mySerializable;

public class AddDeviceActivity extends AppCompatActivity {

    public Context _context = this;
    public String  _title = "Add New";

    // View
    private TextView    textView_defaultName,
            textView_MAC, textView_alertMessage;
    private EditText editText_lockName, editText_pin, editText_pinAgain;
    private Button button_save, button_cancel;

    // AsynTask
    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;

    // Models
    private UserData userData;
    private bleLockDevice mLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        setTitle(_title);
        Intent intent = this.getIntent();

        textView_defaultName = (TextView) findViewById(R.id.textView_defaultName);
        textView_MAC = (TextView) findViewById(R.id.textView_MAC);
        textView_alertMessage = (TextView) findViewById(R.id.textView_addAlert02);

        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();

        // Initiate Models
        mLock = new bleLockDevice();
        userData = new UserData();

        // Load Models
        Serializable serial = getIntent().getSerializableExtra("myserial");
        if(serial != null) {
            mySerializable originSerial = (mySerializable) serial;
            mLock = originSerial.getLOCK();
            userData = originSerial.getUserData();
        }

        // Set Text View
        textView_defaultName.setText(mLock.ble_name);
        textView_MAC.setText(mLock.ble_mac);

        button_save = (Button) findViewById(R.id.button_saveAddDevice);
        button_cancel = (Button)findViewById(R.id.button_cancelAddDevice);

        editText_lockName = (EditText)findViewById(R.id.editText_newName);
        editText_pin = (EditText) findViewById(R.id.editText_PIN);
        editText_pinAgain = (EditText) findViewById(R.id.editText_againPIN);

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if not fullfill.
                if(Common.isEmpty(editText_lockName) || Common.isEmpty(editText_pin) || Common.isEmpty(editText_pinAgain)) {
                    textView_alertMessage.setVisibility(View.VISIBLE);
                    textView_alertMessage.setText("* All fields are required");
                    return;
                }

                String sPwd = editText_pin.getText().toString();
                String sRePwd = editText_pinAgain.getText().toString();
                if(sPwd.equals(sRePwd)) {
                    textView_alertMessage.setVisibility(View.GONE);
                } else {
                    textView_alertMessage.setVisibility(View.VISIBLE);
                    textView_alertMessage.setText("* Password does not match");
                    return;
                }
                //Successfull
                //Back to List_Device_Activity
                //exec task register
                new TaskAddDevice().execute(mLock.ble_mac, editText_lockName.getText().toString(),
                        Common.PinToHex(editText_pin.getText().toString()), Integer.toString(userData.getId()));
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Back to List_New_Device_Activity
            }
        });
    }

    public class TaskAddDevice extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgresDialog = ProgressDialog.show(AddDeviceActivity.this, "Please wait", "Device is saving...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("mac", params[0]);
            postParam.put("name", params[1]);
            postParam.put("pin", params[2]);
            postParam.put("status", "active");
            postParam.put("type", "root");
            postParam.put("uid",  params[3]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/AddDevice", postParam);
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
                Toast.makeText(AddDeviceActivity.this, "Saved device success", Toast.LENGTH_LONG).show();
                Intent i = new Intent(_context,ListDeviceActivity.class);
                try {
                    mySerializable desMySerial = new mySerializable();
                    desMySerial.setUserData(userData);
                    i.putExtra(bleDefine.LOCK_DATA, desMySerial);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(AddDeviceActivity.this, "Saved device fail!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
