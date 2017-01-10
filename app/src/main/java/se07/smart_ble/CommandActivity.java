
package se07.smart_ble;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.Serializable;

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
    private Button  button_unlock, button_share, button_history, button_changePass, button_infomation;
    private TextView txt_typeUser;

    // Models
    private UserData userData;
    private LockData lockData;
    public bleLockDevice mLockData;

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

        button_unlock.setText("Click to Lock");
        // Initiate Models
        userData = new UserData();
        lockData = new LockData();

        Serializable serializable = intent.getSerializableExtra(bleDefine.LOCK_DATA);
        if(serializable != null) {
            mySerializable originMySerial = (mySerializable)serializable;
            this.mLockData = originMySerial.getLOCK();
            this.lockData = originMySerial.getLockData();
            this.userData = originMySerial.getUserData();
        }

        button_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(islocked) {
                    islocked = false;
                    mLockData.sendCommand("32");
                    button_unlock.setText("Click to Lock");
                }
                else{
                    mLockData.sendCommand("33");
                    islocked = true;
                    button_unlock.setText("Click to Unclock");
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
    }
    private void showDialog_loginChangePin(){
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("Change Lock PIN");

        View viewInflated = LayoutInflater.from(_context).inflate(R.layout.dialog_change_pincode_01, null);

        builder.setView(viewInflated);
        final AlertDialog dialog = builder.create();

        //Remove Button
        Button button_changePinNext = (Button) viewInflated.findViewById(R.id.button_changePinNext);
        button_changePinNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(_TITLE,"SAVED!!!");
                Toast.makeText(_context, "Saved Successfully!",
                        Toast.LENGTH_LONG).show();
                showDialog_changePin();
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

        final EditText editText_newPin = (EditText) viewInflated.findViewById(R.id.editText_newPIN);
        EditText editText_newPinAgain = (EditText) viewInflated.findViewById(R.id.editText_againPIN);

        builder.setView(viewInflated);
        final AlertDialog dialog = builder.create();

        //Remove Button
        Button button_newPinSave = (Button) viewInflated.findViewById(R.id.button_newPinSave);
        button_newPinSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(_TITLE,"Change to:"+editText_newPin.getText().toString());
                Toast.makeText(_context, "Changed Successfully!",
                        Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });

        Button button_newPinCancel = (Button) viewInflated.findViewById(R.id.button_newPinCancel);
        button_newPinCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
