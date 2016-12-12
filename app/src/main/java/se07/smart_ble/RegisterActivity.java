package se07.smart_ble;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;

import android.content.Context;
import android.os.AsyncTask;
import java.io.OutputStream;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.widget.Toast;
import android.content.Intent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    public Context _context = this;
    public String _title = "Create An Account";

    private EditText editText_name, editText_email, editText_pwd, editText_rePwd;
    private Button button_register, button_cancel;
    private TextView textView_alertMessage;

    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle(_title);

        // Edit text
        editText_name = (EditText) findViewById(R.id.editText_yourName);
        editText_email = (EditText) findViewById(R.id.editText_yourEmail);
        editText_pwd  = (EditText) findViewById(R.id.editText_password);
        editText_rePwd = (EditText) findViewById(R.id.editText_againPassword);
        // Button
        button_register = (Button) findViewById(R.id.button_register);
        button_cancel = (Button) findViewById(R.id.button_cancelRegister);
        // alert message
        textView_alertMessage = (TextView) findViewById(R.id.textView_registerAlert);

        //
        m_AccessServiceAPI = new AccessServiceAPI();

        // Trigger click button events
        button_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if not fullfill.
                if(isEmpty(editText_name) || isEmpty(editText_email) || isEmpty(editText_pwd) || isEmpty(editText_rePwd)) {
                    textView_alertMessage.setVisibility(View.VISIBLE);
                    textView_alertMessage.setText("* All fiel are required");
                    return;
                }

                // check if password does not match.
                String sPwd = editText_pwd.getText().toString();
                String sRePwd = editText_rePwd.getText().toString();
                if(sPwd.equals(sRePwd)) {
                    textView_alertMessage.setVisibility(View.GONE);
                } else {
                    textView_alertMessage.setVisibility(View.VISIBLE);
                    textView_alertMessage.setText("* Password does not match");
                    return;
                }

                //exec task register
                new TaskRegister().execute(editText_name.getText().toString(),
                        editText_email.getText().toString(), editText_pwd.getText().toString());
            }
        });
    }

    // Utilities
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }

    public class TaskRegister extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgresDialog = ProgressDialog.show(RegisterActivity.this, "Please wait", "Registration processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("user_name", params[0]);
            postParam.put("email", params[1]);
            postParam.put("password", params[2]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/register", postParam);
                JSONObject jsonObject = new JSONObject(jsonString);
                return jsonObject.getInt("result");
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
                Toast.makeText(RegisterActivity.this, "Registration success", Toast.LENGTH_LONG).show();
                Intent i = new Intent();
                //i.putExtra("username", txtUsername.getText().toString());
                //i.putExtra("password", txtPassword1.getText().toString());
                //setResult(1, i);
                //finish();
            } else if(integer == Common.RESULT_USER_EXISTS) {
                Toast.makeText(RegisterActivity.this, "Username is exists!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Registration fail!", Toast.LENGTH_LONG).show();
            }
        }
    }
}