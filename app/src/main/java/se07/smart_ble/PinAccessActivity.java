package se07.smart_ble;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;

import se07.smart_ble.API.AccessServiceAPI;
import se07.smart_ble.API.Common;
import se07.smart_ble.Models.History;
import se07.smart_ble.Models.LockData;
import se07.smart_ble.Models.UserData;
import se07.smart_ble.Serializable.mySerializable;


public class PinAccessActivity extends AppCompatActivity {

    public Context _context = this;
    private Intent intent;
    private String _title = "PIN Access";
    private TextView    textView_num1, textView_num2, textView_num3, textView_num4;
    private String _keyboard[]= {"1","2","3","4","5","6","7","8","9","BACK","0", ""};
    private int _maxsize = 4;
    private int countString = 0;
    private String _lockPIN = "";
    private GridView gridView_keyboard;
    private ArrayList<TextView> arrayEditText = new ArrayList<TextView>();

    // Models object
    private bleLockDevice mLockData;
    private LockData lockData;
    private UserData userData;

    // AsynTask
    private ProgressDialog m_ProgresDialog;
    private AccessServiceAPI m_AccessServiceAPI;
    private JSONObject jsonData;

    //demo code
    private int demoUID = 15;

    // PHash
    private byte[] bytePHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_access);

        // Set title
        setTitle(_title);

        // get intent
        intent = this.getIntent();

        // AccessService
        m_AccessServiceAPI = new AccessServiceAPI();
        // Json Object
        jsonData = new JSONObject();

        // Initiate models
        this.mLockData = new bleLockDevice();
        this.lockData = Common.GenerateLockData();
        this.userData = Common.GenerateUserData();

        // Load Models
        Serializable serializable = intent.getSerializableExtra(bleDefine.LOCK_DATA);
        if(serializable != null) {
            mySerializable originMySerial = (mySerializable)serializable;
            this.mLockData = originMySerial.getLOCK();
            this.lockData = originMySerial.getLockData();
            this.userData = originMySerial.getUserData();
        }

        // initiate textview digits
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
//                        if(_lockPIN.equals("1234")){
//                            //mLockData.sendCommand("32");
//                            Intent i = new Intent(_context, CommandActivity.class);
//                            mySerializable desMySerial = new mySerializable();
//                            desMySerial.setLockData(lockData);
//                            desMySerial.setUserData(userData);
//                            desMySerial.setBleLockDevice(mLockData);
//                            i.putExtra(bleDefine.LOCK_DATA, desMySerial);
//                            startActivity(i);
//                        }
//                        else
//                        {
//                            countString=0;
//                            _lockPIN = "";
//                        }

                        if(countString < _maxsize) {
                            countString=0;
                            _lockPIN = "";
                            ResetArrayEditText();
                        } else {
                            new TaskCheckPin().execute(bleDefine.bytesToHex(bytePHash));
                        }

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
                            if(countString == _maxsize) {
                                bytePHash = getPHASH();
                            }
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLockData.gatt != null)
            mLockData.gatt.close();
    }

    private void ResetArrayEditText () {
        for (TextView edt : arrayEditText) {
            edt.setText("");
        }
    }

    private byte[] getPHASH(){

        byte[] preHash = get_preHash();
        byte[] UserData = getUserData();
        Log.d(_title,"PreHash: " + bleDefine.bytesToHex(preHash));
        byte[] PHash = new byte[24];
        int dif = PHash.length - UserData.length;
        for(int i =0; i < preHash.length; i++)
        {
            PHash[i] = preHash[i];
        }
        for(int i = dif; i < 24; i++){
            PHash[i] = UserData[i-dif];
        }
        Log.d(_title,"PHASH: " + bleDefine.bytesToHex(PHash));
        return PHash;
    }

    private byte[] get_preHash() {
        byte[] b_m_key = getPinHash();
        byte[] b_m_data = getPinData();
        aes.aes_enc_dec(b_m_data, b_m_key, (byte) 0);
        Log.d(_title, "ENC: " + bleDefine.bytesToHex(b_m_data));
        return b_m_data;
    }

    //Same in server
    private byte[] getPinHash(){
        byte[] bytePinHash = new byte[16];
        byte[] bytePIN = bleDefine.hexToBytes(Common.PinToHex(_lockPIN));
        for(int i=0; i<4; i++ ){
            bytePinHash[i*4]=bytePIN[i];
        }

        for(int i=0; i<16; i++){
            if(i%4 != 0){
                if(i%2 != 0)
                    bytePinHash[i] = (byte)0x51; //Q
                else
                    bytePinHash[i] = (byte)0x53; //S
            }
        }

        Log.d(_title,"PIN HASH: " + bleDefine.bytesToHex(bytePinHash));
        return bytePinHash;
    }

    //P_DATA = 12 byte data + 4
    private byte[] getPinData(){
        byte[] byteResult = new byte[16];
        int index = 0;
        //Command 2
        byte[] byteCommand = bleDefine.hexToBytes(bleDefine.CMD_UNLOCK);
        for(int i=0; i < 2; i++)
            byteResult[index++]= byteCommand[i];

        //PIN 4
        byte[] bytePIN = bleDefine.hexToBytes(Common.PinToHex(_lockPIN));
        for(int i=0; i<4; i++ ){
            byteResult[index++]=bytePIN[i];
        }

        //Random 4
        byte[] byteRandom = getRandomBytes(4);
        for(int i=0; i < 4; i++)
            byteResult[index++]= byteRandom[i];

        //SK 6
        //byte[] byteSK = bleDefine.hexToBytes(mLockData.ble_sk);
        byte[] byteSK = lockData.getSessionKey();
        for(int i=0; i < byteSK.length; i++){
            byteResult[index++] = byteSK[i];
        }
        Log.d(_title,"PIN DATA: " + bleDefine.bytesToHex(byteResult));
        return byteResult;
    }

    private byte[] getUserData(){

        byte[] byteUserData = new byte[8];
        int index=0;
//        if(mLockData == null){
//            return null;
//        }

        //MAC 6
        //byte[] byteMac = bleDefine.MacToBytes(mLockData.ble_mac);
        byte[] byteMac = bleDefine.MacToBytes(lockData.get_mMAC());
        for(int i=0; i< byteMac.length; i++)
        {
            byteUserData[index++] = byteMac[i];
        }

        //UID 2
        String hexUID4Digits = String.format("%04X", userData.getId()); // 2 bytes = 4 hex digits
        byte[] byteUID = bleDefine.hexToBytes(hexUID4Digits);
        for(int i=0; i < byteUID.length; i++){
            byteUserData[index++] = byteUID[i];
        }
        return byteUserData;
    }

    private byte[] getRandomBytes(int length){

        byte[] byteResult = new byte[length];
        for(int i = 0; i < length; i++)
        {
            Random r = new Random();
            int n = r.nextInt(255);
            byteResult[i]= (byte)n;
        }
        return byteResult;
    }

    public class TaskCheckPin extends AsyncTask<String, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            m_ProgresDialog = ProgressDialog.show(PinAccessActivity.this, "Please wait", "Server is change PIN...", true);
        }

        @Override
        protected Integer doInBackground(String... params) {
            Map<String, String> postParam = new HashMap<>();
            postParam.put("phash", params[0]);
            try{
                String jsonString = m_AccessServiceAPI.getJSONStringWithParam_POST(Common.SERVICE_API_URL + "/converthex", postParam);
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
                try {
                    if(lockData.IsInBound) {
                        mLockData.sendCommand("32");
                        // Save hictory
                        History his = new History(_context, userData.getId(), lockData.getLockId(), "unlock");
                        his.SaveHistory();
                    }

                    // Chuyen activity
                    Intent i = new Intent(_context, CommandActivity.class);
                    mySerializable desMySerial = new mySerializable();
                    desMySerial.setLockData(lockData);
                    desMySerial.setUserData(userData);
                    desMySerial.setBleLockDevice(mLockData);
                    i.putExtra(bleDefine.LOCK_DATA, desMySerial);
                    startActivity(i);
                } catch (Exception e) {
                    Log.v("Exception", e.toString());
                }
            } else if (integer == Common.pin_not_correct_code) {
                Toast.makeText(PinAccessActivity.this, "PIN is not correct!", Toast.LENGTH_LONG).show();
                countString=0;
                _lockPIN = "";
                ResetArrayEditText();
            } else {
                Toast.makeText(PinAccessActivity.this, "Failed to connect to server!", Toast.LENGTH_LONG).show();
                countString=0;
                _lockPIN = "";
                ResetArrayEditText();
            }
        }
    }
}

