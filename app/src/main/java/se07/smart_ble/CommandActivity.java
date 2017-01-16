
package se07.smart_ble;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Models.History;
import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.Serializable.mySerializable;

public class CommandActivity extends AppCompatActivity {

    private Context _context = this;

    // Intent
    private Intent intent;

    // Title
    private String _TITLE = "LOCK DEMO";

    // Views
    private Button  button_unlock, button_share, button_history, button_changePass, button_infomation, button_remove;
    private TextView txt_typeUser;

    // Models
    private UserData userData;
    private LockData lockData;
    public bleLockDevice mLockData;

    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;

    private boolean islocked= false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);

        // Set title
        setTitle(_TITLE);

        // get intent
        intent = this.getIntent();
        this.mLockData = new bleLockDevice();

        txt_typeUser = (TextView)findViewById(R.id.textView_typeUser);
        // get Views
        button_unlock = (Button) findViewById(R.id.button_unlock);
        button_share = (Button) findViewById(R.id.button_share);
        button_history = (Button) findViewById(R.id.button_history);
        button_changePass = (Button) findViewById(R.id.button_changePass);
        button_infomation = (Button) findViewById(R.id.button_information);
        button_remove = (Button) findViewById(R.id.btn_removeLock);

        // Initiate Models
        userData = new UserData();
        lockData = new LockData();

        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();

        Serializable serializable = intent.getSerializableExtra(bleDefine.LOCK_DATA);
        if(serializable != null) {
            mySerializable originMySerial = (mySerializable)serializable;
            this.mLockData = originMySerial.getLOCK();
            this.lockData = originMySerial.getLockData();
            this.userData = originMySerial.getUserData();
        }

        button_unlock.setText("Click to Lock");
        button_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(islocked) {
                    islocked = false;
                    mLockData.sendCommand("32");
                    button_unlock.setText("Click to Lock");
                    History his = new History(_context, userData.getId(), lockData.getLockId(), "unlock");
                    his.SaveHistory();
                }
                else{
                    mLockData.sendCommand("33");
                    islocked = true;
                    button_unlock.setText("Click to Unclock");
                    History his = new History(_context, userData.getId(), lockData.getLockId(), "lock");
                    his.SaveHistory();
                }
            }
        });

        button_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(_context, ShareActivity.class);
                mySerializable desMySerial = new mySerializable();
                desMySerial.setUserData(userData);
                desMySerial.setLockData(lockData);
                i.putExtra(bleDefine.LOCK_DATA, desMySerial);
                startActivity(i);
            }
        });

        button_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySerializable desMySerial = new mySerializable();
                desMySerial.setBleLockDevice(mLockData);
                desMySerial.setLockData(lockData);
                desMySerial.setUserData(userData);
                Intent i = new Intent(_context, HistoryActivity.class);
                i.putExtra(bleDefine.LOCK_DATA, desMySerial);
                startActivity(i);
            }
        });

        button_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(_TITLE,"Remove Confirm!!!");
                new AlertDialog.Builder(_context)
                    .setTitle("Remove All Owners")
                    .setMessage("Are you sure you want to remove all owners?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(_TITLE,"Removed!!!");
                            new TaskRemoveAllOwner().execute(String.valueOf(lockData.getLockId()));
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Log.d(_TITLE,"Remove All Owners Canceling!!!");
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        });


        button_changePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog_loginChangePin();
            }
        });

        button_infomation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if(lockData.IsInBound) {
            button_unlock.setVisibility(View.VISIBLE);
        } else {
            button_unlock.setVisibility(View.GONE);
        }
        new TaskCheckTypeUser().execute(Integer.toString(userData.getId()), Integer.toString(lockData.getLockId()));
    }
    private void showDialog_loginChangePin(){
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Change Lock PIN");

        View viewInflated = LayoutInflater.from(_context).inflate(R.layout.dialog_change_pincode_01, null);
        builder.setView(viewInflated);
        final AlertDialog dialog = builder.create();

        // GetView
        // EditText Email
        EditText editText_rootEmail = (EditText) viewInflated.findViewById(R.id.editText_rootEmail);
        // EditText Password
        final EditText editTextPassword = (EditText) viewInflated.findViewById(R.id.editText_rootPassword);
        // Button Next
        Button button_changePinNext = (Button) viewInflated.findViewById(R.id.button_changePinNext);

        // Load View
        editText_rootEmail.setText(userData.getEmail());

        // Trigger Event Click
        button_changePinNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskCheckLogin().execute(userData.getEmail(), editTextPassword.getText().toString());
                dialog.dismiss();
            }
        });

        Button button_changePinCancel = (Button) viewInflated.findViewById(R.id.button_changePinCancel);
        button_changePinCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showDialog_changePin(){

        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Change Lock PIN");

        View viewInflated = LayoutInflater.from(_context).inflate(R.layout.dialog_change_pincode_02, null);

        // Get View
        // EditText New Pin
        final EditText editText_newPin = (EditText) viewInflated.findViewById(R.id.editText_newPIN);
        // EditText Pin Again
        final EditText editText_newPinAgain = (EditText) viewInflated.findViewById(R.id.editText_newPinAgain);
        // Button Save
        Button button_newPinSave = (Button) viewInflated.findViewById(R.id.button_newPinSave);
        // Button Cancel
        Button button_newPinCancel = (Button) viewInflated.findViewById(R.id.button_newPinCancel);

        builder.setView(viewInflated);
        final AlertDialog dialog = builder.create();

        // Trigger Event Clicks
        button_newPinSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if pin matches
                String sPwd = editText_newPin.getText().toString();
                String sRePwd = editText_newPinAgain.getText().toString();
                if(!sPwd.equals(sRePwd)) {
                    Toast.makeText(_context, "Pin does not match",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if(sPwd.length() != 4) {
                    Toast.makeText(_context, "Pin requires 4 digits",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                new TaskChangePin().execute(Integer.toString(userData.getId()),
                        Integer.toString(lockData.getLockId()), Common.PinToHex(editText_newPin.getText().toString()));
                dialog.dismiss();
            }
        });
        button_newPinCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public class TaskRemoveAllOwner extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("lock_id", params[0]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/RemoveAllLockOwner", postParam);
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
            if(integer == Common.result_success) {
                try {
                    Log.d(_TITLE,"SAVED!!!");
                    Toast.makeText(_context, "Remove all owners successfully!",
                            Toast.LENGTH_LONG).show();
                    // start listdevice activity
                    mySerializable desMySerial = new mySerializable();
                    desMySerial.setUserData(userData);
                    Intent i = new Intent(_context,ListDeviceActivity.class);
                    i.putExtra(bleDefine.LOCK_DATA, desMySerial);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(_context, "Failed to remove all owners!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class TaskCheckTypeUser extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("user_id", params[0]);
            postParam.put("lock_id", params[1]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/CheckTypeUser", postParam);
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
            if(integer == Common.result_success) {
                try {
                    String userType = jsonData.getString("type");
                    txt_typeUser.setText(userType.toUpperCase());
                    if(userType.equals("root")) {
                        button_share.setVisibility(View.VISIBLE);
                        button_history.setVisibility(View.VISIBLE);
                        button_changePass.setVisibility(View.VISIBLE);
                        button_infomation.setVisibility(View.VISIBLE);
                        button_remove.setVisibility(View.VISIBLE);
                    } else {
                        button_share.setVisibility(View.GONE);
                        button_history.setVisibility(View.GONE);
                        button_changePass.setVisibility(View.GONE);
                        button_infomation.setVisibility(View.GONE);
                        button_remove.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            }
        }
    }

    public class TaskChangePin extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("user_id", params[0]);
            postParam.put("lock_id", params[1]);
            postParam.put("new_pin", params[2]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/ChangePin", postParam);
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
            if(integer == Common.result_success) {
                try {
                    Toast.makeText(_context, "Saved PIN successfully",
                            Toast.LENGTH_LONG).show();

                    // start listdevice activity
                    mySerializable desMySerial = new mySerializable();
                    desMySerial.setUserData(userData);
                    Intent i = new Intent(_context,ListDeviceActivity.class);
                    i.putExtra(bleDefine.LOCK_DATA, desMySerial);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(_context, "Failed to connect to server",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    public class TaskCheckLogin extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("email", params[0]);
            postParam.put("password", params[1]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/Login", postParam);
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
            if(integer == Common.login_success_code) {
                try {
                    // open change pin dialog
                    showDialog_changePin();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else if (integer == Common.user_not_existing_code) {
                Toast.makeText(_context, "Password is not correct!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(_context, "Failed to connect server!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
