package se07.smart_ble;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.Serializable.mySerializable;

public class ShareActivity extends AppCompatActivity {

    private Context _context = this;
    private String _TITLE = "SHARE MANAGEMENT";

    // intent
    private Intent intent;

    private ArrayList<String> listCurrentUser = new ArrayList<String>();

    private Button  button_search;
    private EditText editText_search;
    private ListView listView_currentUser;
    private TextView textView_result01, textView_result02;

    private static final String[] array_typeAccess = new String[]{"Owner", "Root"};

    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;
    // list owners
    private JSONArray jsonArrayOwner;

    // Dialog add new owner
    private AlertDialog dialogAddOwner;
    private TextView dgAddOwner_TextView_email;
    private Spinner dgAddOwner_Spinner_type;

    // Models
    private UserData userData;
    private LockData lockData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // get intent
        intent = this.getIntent();

        // Set view
        button_search = (Button) findViewById(R.id.button_search);
        editText_search = (EditText) findViewById(R.id.editText_search);
        listView_currentUser = (ListView) findViewById(R.id.listView_currentOwner);
        textView_result01 = (TextView) findViewById(R.id.textView_searchResult01);
        textView_result02 = (TextView) findViewById(R.id.textView_searchResult02);

        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();
        // list owner
        jsonArrayOwner = new JSONArray();

        // Initiate Models
        userData = new UserData();
        lockData = new LockData();

        // Load models
        Serializable serial = intent.getSerializableExtra("myserial");
        if(serial != null) {
            mySerializable originMySerial = (mySerializable) serial;
            userData = originMySerial.getUserData();
            lockData = originMySerial.getLockData();
        }


        // 1. get mac address
        //String mac = intent.getStringExtra("mac");
        // 2. task get current owners excute
        // new TaskGetCurrentOwners().execute(mac);
        // demo
        new TaskGetCurrentOwners().execute(lockData.get_mMAC());

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskSearchUser().execute(editText_search.getText().toString());
            }
        });

        listView_currentUser.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDialog_editUserPermission(position);
            }
        });

        textView_result01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog_addNew(textView_result01.getText().toString());
            }
        });

        textView_result02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog_addNew(textView_result02.getText().toString());
            }
        });
    }

    private void showDialog_editUserPermission(int index){
        try {
            // get selected owner
            final JSONObject selectedOwner = jsonArrayOwner.getJSONObject(index);
            // Initiate Edit Owner Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(_context);
            builder.setTitle("Edit owner - " + lockData.get_mName());
            // Set layout to dialog
            final View viewInflated = LayoutInflater.from(_context).inflate(R.layout.dialog_edit_permission, null);
            builder.setView(viewInflated);
            // Textview email
            final TextView textView_editOwnerEmail = (TextView) viewInflated.findViewById(R.id.textView_editOwnerEmail);
            textView_editOwnerEmail.setText(selectedOwner.getString("email"));
            //Spinner
            final Spinner spinner_typeAccess = (Spinner) viewInflated.findViewById(R.id.spinner_editTypeAccess);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>( _context,
                    android.R.layout.simple_spinner_dropdown_item, array_typeAccess);
            spinner_typeAccess.setAdapter(adapter);
            int spiner_selected_index = selectedOwner.getString("user_type").equals("root") ? 1 : 0;
            spinner_typeAccess.setSelection(spiner_selected_index, true);
            final AlertDialog dialog = builder.create();

            //Remove Button
            Button button_removeOwner = (Button) viewInflated.findViewById(R.id.button_removeOwner);
            button_removeOwner.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(_TITLE,"Remove Confirm!!!");
                    new AlertDialog.Builder(_context)
                            .setTitle("Remove Owner")
                            .setMessage("Are you sure you want to remove this owner?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(_TITLE,"Removed!!!");
                                    new TaskRemoveOwner().execute(textView_editOwnerEmail.getText().toString(), lockData.get_mMAC());
                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d(_TITLE,"Remove Canceling!!!");
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    dialog.dismiss();

                }
            });

            Button button_editSave = (Button) viewInflated.findViewById(R.id.button_editSave);
            button_editSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(_TITLE,"SAVED!!!");
                    new TaskEditOwner().execute(textView_editOwnerEmail.getText().toString(), lockData.get_mMAC(),
                            spinner_typeAccess.getSelectedItem().toString());
                    dialog.dismiss();
                }
            });

            Button button_editCancel = (Button) viewInflated.findViewById(R.id.button_editCancel);
            button_editCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception e) {
            Log.v("Edit Owner", e.toString());
        }

    }

    private void showDialog_addNew(final String newEmail){
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Add new owner - " + lockData.get_mName());

        View viewInflated = LayoutInflater.from(_context).inflate(R.layout.dialog_add_new_owner, null);

        builder.setView(viewInflated);
        dgAddOwner_Spinner_type = (Spinner) viewInflated.findViewById(R.id.spinner_typeAccess);
        dgAddOwner_TextView_email = (TextView)  viewInflated.findViewById(R.id.textView_userEmail);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(
                        _context,
                        android.R.layout.simple_spinner_dropdown_item,
                        array_typeAccess);
        dgAddOwner_Spinner_type.setAdapter(adapter);
        dgAddOwner_TextView_email.setText(newEmail);

        dialogAddOwner = builder.create();

        //Remove Button
        Button button_addSave = (Button) viewInflated.findViewById(R.id.button_addSave);
        button_addSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TaskShare().execute(newEmail, lockData.get_mMAC(), dgAddOwner_Spinner_type.getSelectedItem().toString());
            }
        });

        Button button_addCancel = (Button) viewInflated.findViewById(R.id.button_addCancel);
        button_addCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogAddOwner.dismiss();
            }
        });
        dialogAddOwner.show();
    }

    public class TaskGetCurrentOwners extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgresDialog = ProgressDialog.show(ShareActivity.this, "Please wait", "Get owner processing...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("mac", params[0]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/GetOwners", postParam);
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
                Toast.makeText(ShareActivity.this, "Get owners success", Toast.LENGTH_LONG).show();
                try {
                    ArrayList<String> dummy_data = new ArrayList<String>();
                    // get list owners
                    jsonArrayOwner = jsonData.getJSONArray("data");
                    for (int index = 0; index < jsonArrayOwner.length(); index++) {
                        JSONObject jsonOwner = jsonArrayOwner.getJSONObject(index);
                        dummy_data.add(jsonOwner.getString("email"));
                    }
                    ArrayAdapter<String> arrayAdapter =
                            new ArrayAdapter<String>(
                                    _context,
                                    android.R.layout.simple_list_item_1,
                                    dummy_data);
                    listView_currentUser.setAdapter(arrayAdapter);
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(ShareActivity.this, "Get Owners fail!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class TaskSearchUser extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("info", params[0]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/SearchUser", postParam);
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
                    textView_result01.setVisibility(View.GONE);
                    textView_result02.setVisibility(View.GONE);
                    ArrayList<String> dummy_data = new ArrayList<String>();
                    // get list owners
                    JSONArray jsonArrayOwner = jsonData.getJSONArray("data");
                    for (int index = 0; index < jsonArrayOwner.length(); index++) {
                        JSONObject jsonOwner = jsonArrayOwner.getJSONObject(index);
                        if(index == 0) {
                            textView_result01.setVisibility(View.VISIBLE);
                            textView_result01.setText(jsonOwner.getString("email"));
                        }
                        if(index == 1) {
                            textView_result02.setVisibility(View.VISIBLE);
                            textView_result02.setText(jsonOwner.getString("email"));
                        }
                    }
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                textView_result01.setVisibility(View.GONE);
                textView_result02.setVisibility(View.GONE);
                Toast.makeText(ShareActivity.this, "Search users fail!", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class TaskShare extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("email", params[0]);
            postParam.put("mac", params[1]);
            postParam.put("type", params[2]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/AddOwner", postParam);
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
                    Toast.makeText(_context, "Saved Successfully!",
                            Toast.LENGTH_LONG).show();
                    dialogAddOwner.dismiss();
                    // load lại list current owner
                    new TaskGetCurrentOwners().execute(lockData.get_mMAC());
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                if(integer == Common.user_owns_lock) {
                    Toast.makeText(ShareActivity.this, "User has owned the lock!", Toast.LENGTH_LONG).show();
                    dialogAddOwner.dismiss();
                } else {
                    Toast.makeText(ShareActivity.this, "Share owner fail!", Toast.LENGTH_LONG).show();
                    dialogAddOwner.dismiss();
                }
            }
        }
    }

    public class TaskRemoveOwner extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("email", params[0]);
            postParam.put("mac", params[1]);
            //postParam.put("type", params[2]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/RemoveOwner", postParam);
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
                    Toast.makeText(_context, "Removed Owner Successfully!",
                            Toast.LENGTH_LONG).show();
                    // load lại list current owner
                    new TaskGetCurrentOwners().execute(lockData.get_mMAC());
                    dialogAddOwner.dismiss();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(_context, "Removed Owner failed!",
                        Toast.LENGTH_LONG).show();
                dialogAddOwner.dismiss();
            }
        }
    }

    public class TaskEditOwner extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("email", params[0]);
            postParam.put("mac", params[1]);
            postParam.put("type", params[2]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/EditOwner", postParam);
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
                    Toast.makeText(_context, "Updated Owner Successfully!",
                            Toast.LENGTH_LONG).show();
                    // load lại list current owner
                    new TaskGetCurrentOwners().execute(lockData.get_mMAC());
                    dialogAddOwner.dismiss();
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else {
                Toast.makeText(_context, "Updated Owner failed!",
                        Toast.LENGTH_LONG).show();
                dialogAddOwner.dismiss();
            }
        }
    }
}
