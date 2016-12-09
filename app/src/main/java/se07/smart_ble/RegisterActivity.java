package se07.smart_ble;

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

public class RegisterActivity extends AppCompatActivity {

    public Context _context = this;
    public String _title = "Create An Account";

    private EditText name, email, pwd, rePwd;
    private Button register, cancel;
    private TextView alertMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        setTitle(_title);

        // Edit text
        name = (EditText) findViewById(R.id.editText_yourName);
        email = (EditText) findViewById(R.id.editText_yourEmail);
        pwd  = (EditText) findViewById(R.id.editText_password);
        rePwd = (EditText) findViewById(R.id.editText_againPassword);
        // Button
        register = (Button) findViewById(R.id.button_register);
        cancel = (Button) findViewById(R.id.button_cancelRegister);
        // alert message
        alertMessage = (TextView) findViewById(R.id.textView_registerAlert);

        // Trigger click button events
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check if not fullfill.
                if(isEmpty(name) || isEmpty(email) || isEmpty(pwd) || isEmpty(rePwd)) {
                    alertMessage.setVisibility(View.VISIBLE);
                    alertMessage.setText("* Please fill all the fiels");
                    return;
                }

                // check if password does not match.
                String sPwd = pwd.getText().toString();
                String sRePwd = rePwd.getText().toString();
                if(sPwd.equals(sRePwd)) {
                    alertMessage.setVisibility(View.GONE);
                } else {
                    alertMessage.setVisibility(View.VISIBLE);
                    alertMessage.setText("* Password does not match");
                    return;
                }
            }
        });
    }

    // Utilities
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0)
            return false;

        return true;
    }
}