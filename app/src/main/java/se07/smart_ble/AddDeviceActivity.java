package se07.smart_ble;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class AddDeviceActivity extends AppCompatActivity {

    public Context _context = this;
    public String  _title = "Add New";

    private mySerializable mSerializable;
    private TextView    textView_defaultName,
                        textView_MAC;
    private bleLockDevice mLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        setTitle(_title);
//        Intent intent = this.getIntent();
//        mSerializable = (mySerializable) intent.getSerializableExtra("mData");
//
//        textView_defaultName = (TextView) findViewById(R.id.textView_defaultName);
//        textView_MAC = (TextView) findViewById(R.id.textView_MAC);
//
//        if(mSerializable != null){
//            mLock = mSerializable.getLOCK();
//            textView_defaultName.setText(mLock.ble_name);
//            textView_MAC.setText(mLock.ble_mac);
//        }


    }
}
