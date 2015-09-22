package interview.com.fruitsbenefits;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class FruitListActivity extends Activity {

    public static final String url = "https://api.myjson.com/bins/3maj0";
    public static final String TAG = "FruitListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fruit_list);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if(!pref.getBoolean("db_populated", false)) {
            Log.d(TAG, "Data not downloaded yet!! Download now...");
            new ContentDownloaderTask().execute(url);
        } else {
            Log.d(TAG, "Data available in DB, no need to download again");
        }
    }

    private class ContentDownloaderTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            InputStream isr = null;
            int size = 30000;
            String dest = strings[0];
            Log.d(TAG, "doInBackground()"+url);
            try {
                URL url = new URL(dest);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                int respCode = connection.getResponseCode();
                Log.d(TAG, "Response code is "+respCode);
                if(respCode != 200)
                    return null;
                isr = connection.getInputStream();
                String responseString = readIt(isr, size);
                Log.d(TAG, "response in string "+responseString.length());
                JSONObject jsonObject = new JSONObject(responseString);
                JSONArray array = jsonObject.getJSONArray("Fruits");
                for(int i= 0; i < array.length(); i++ ) {
                    JSONObject obj = array.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    String name = obj.getString("name");
                    cv.put(FruitBenefitsProvider.NAME, name);
                    Log.d(TAG, "name: " + name);
                    String description = obj.getString("description");
                    cv.put(FruitBenefitsProvider.DESC, description);
                    //Log.d(TAG, "description: " + description);
                    getContentResolver().insert(FruitBenefitsProvider.CONTENT_URI, cv);
                }
                SharedPreferences sp = PreferenceManager
                        .getDefaultSharedPreferences(FruitListActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("db_populated", true).commit();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        private String readIt(InputStream is, int len) throws IOException {
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String readline;
            while((readline = br.readLine()) != null) {
                sb.append(readline);
            }
            return sb.toString();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfig change");
    }
}
