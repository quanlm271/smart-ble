package se07.smart_ble;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by QuanLM on 10-Dec-16.
 */
public class myAsyncTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String url = "http://10.10.10.10:8081/register";

        HttpURLConnection httpcon = null;
        try {
            httpcon = (HttpURLConnection) new URL(url)
                    .openConnection();
        } catch (MalformedURLException e1) {
            System.out.println("P11" + e1);
        } catch (IOException e1) {
            System.out.println("P1" + e1);
        }

        try {
            httpcon.setDoOutput(true);
            httpcon.setRequestProperty("Content-Type", "application/json");
            httpcon.setRequestProperty("Accept", "application/json");
            httpcon.setRequestMethod("POST");
            httpcon.setDoInput(true);
            httpcon.setDoOutput(true);
            httpcon.connect();
            byte[] outputBytes;
            JSONObject obj = new JSONObject();
            obj.put("user_nane", "minhquan");
            obj.put("password", "123456");
            outputBytes = obj.toString().getBytes("UTF-8");
            OutputStream os = httpcon.getOutputStream();
            os.write(outputBytes);
            os.close();

            int responseCode=httpcon.getResponseCode();
            Log.d("Respon","Res "+responseCode);
        } catch (Exception ex) {
            System.out.println("HHA"+ex);
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        System.out.println("AA"+s);
    }
}
