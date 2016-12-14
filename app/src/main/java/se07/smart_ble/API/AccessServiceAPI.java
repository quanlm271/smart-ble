package se07.smart_ble.API;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.lang.Object;

/**
 * Created by SangPham on 12/12/2016.
 */

public class AccessServiceAPI {
    /**
     * Get json string from URL with method POST
     * @param serviceUrl
     * @param params post data
     * @return json string
     */
    public String getJSONStringWithParam_POST(String serviceUrl, Map<String, String> params)
            throws IOException
    {
        String jsonString = null;
        HttpURLConnection conn = null;
        String line;

        URL url;
        try
        {
            url = new URL(serviceUrl);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("invalid url: " + serviceUrl);
        }

        JSONObject body = new JSONObject(params);
        byte[] bytes = body.toString().getBytes("UTF-8");
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();

            Log.w("getJSONStringWithParam", "Response Status = " + status);
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }

            BufferedReader  bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();


            while ((line = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(line + '\n');
            }

            jsonString = stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            conn.disconnect();
        }

        return jsonString;
    }
}
