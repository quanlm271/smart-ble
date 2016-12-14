package se07.smart_ble;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddDeviceActivity extends AppCompatActivity {

    public Context _context = this;
    public String  _title = "Add New";

    private mySerializable mSerializable;
    private TextView    textView_defaultName,
            textView_MAC, textView_alertMessage;
    private bleLockDevice mLock;
    private EditText editText_lockName, editText_pin, editText_pinAgain;

    private Button button_save, button_cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        setTitle(_title);
        Intent intent = this.getIntent();
        mSerializable = (mySerializable) intent.getSerializableExtra("mData");

        textView_defaultName = (TextView) findViewById(R.id.textView_defaultName);
        textView_MAC = (TextView) findViewById(R.id.textView_MAC);
        textView_alertMessage = (TextView) findViewById(R.id.textView_addAlert02);

        if(mSerializable != null){
            bleLockDevice mLockData = mSerializable.getLOCK();
            textView_defaultName.setText(mLockData.ble_name);
            textView_MAC.setText(mLockData.ble_mac);
        }else
        {
            //Dummy Data
            mLock = new bleLockDevice("lock_01","88:C2:55:12:34:5A");
        }

        button_save = (Button) findViewById(R.id.button_saveAddDevice);
        button_cancel = (Button)findViewById(R.id.button_cancelAddDevice);

        editText_lockName = (EditText)findViewById(R.id.editText_newName);
        editText_pin = (EditText) findViewById(R.id.editText_PIN);
        editText_pinAgain = (EditText) findViewById(R.id.editText_againPIN);

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // check if not fullfill.
                if(isEmpty(editText_lockName) || isEmpty(editText_pin) || isEmpty(editText_pinAgain)) {
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
            }
        });

        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Back to List_New_Device_Activity
            }
        });
    }

    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }
}
