package se07.smart_ble;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class PinAccessActivity extends AppCompatActivity {

    public Context _context = this;
    private String _title = "PIN Access";
    private TextView    textView_num1,
                        textView_num2,
                        textView_num3,
                        textView_num4;
    private String _keyboard[]= {"1","2","3","4","5","6","7","8","9","BACK","0", ""};
    private int _maxsize = 4;
    private int countString = 0;
    private String _lockPIN = "";
    private GridView gridView_keyboard;
    private ArrayList<TextView> arrayEditText = new ArrayList<TextView>();

    private mySerializable mSerializable;
    private LockData mLockData;

    //demo code
    private int demoUID = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_access);

        setTitle(_title);

        Intent intent = this.getIntent();
        mSerializable = (mySerializable) intent.getSerializableExtra("mData");

        if(mSerializable != null){
            mLockData = mSerializable.getLOCK();
        }

        textView_num1 = (TextView)findViewById(R.id.textView_num1);
        textView_num2 = (TextView)findViewById(R.id.textView_num2);
        textView_num3 = (TextView)findViewById(R.id.textView_num3);
        textView_num4 = (TextView)findViewById(R.id.textView_num4);

        arrayEditText.add(textView_num1);
        arrayEditText.add(textView_num2);
        arrayEditText.add(textView_num3);
        arrayEditText.add(textView_num4);

        gridView_keyboard = (GridView) findViewById(R.id.gridView);
        final ItemAdapter itemAdapter = new ItemAdapter(_context, _keyboard);
        gridView_keyboard.setAdapter(itemAdapter);

        gridView_keyboard.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String keyVal = _keyboard[position];
                switch (keyVal){
                    case "":
                        Intent intent = new Intent(_context, CommandActivity.class);
                        startActivity(intent);
                        break;
                    case "BACK":
                        finish();
                        break;
                    default:
                        if(countString < _maxsize){
                            _lockPIN += keyVal;
                            arrayEditText.get(countString).setText("*");
                            countString++;
                            Log.d(_title,"PIN :" + _lockPIN);
                        }else{
//                            for(TextView textView: arrayEditText){
//                                textView.setText(null);
//                            }
//                            countString = 0;
//                            _lockPIN = "";
                            getPinData();
                        }
                        break;

                }
            }
        });
    }
    private byte[] getPinData(){
        byte[] byteResult = new byte[20];
        int index = 3;
        //PIN
        byte[] bytePIN = _lockPIN.getBytes();
        for(int i=0; i<4; i++ ){
            byteResult[index++]=bytePIN[i];
        }

        for(int i = 0; i < 2; i++)
        {
            Random r = new Random();
            int n = r.nextInt(255);
            byteResult[index++]= (byte)n;
        }
        //MAC
        byte[] byteMac = bleDefine.MacToBytes(mLockData.get_mMAC());
        for(int i=0; i< byteMac.length; i++)
        {
            byteResult[index++] = byteMac[i];
        }
        //USer ID

        Log.d(_title,bleDefine.bytesToHex(byteResult));
        return byteMac;
    }

    private byte[] getUserData(){
        byte[] byteUserData;
        byte[] byteRandom;
//        for(int i=0; i<4; i++)
//        {
//
//        }
        return null;
    }
}

