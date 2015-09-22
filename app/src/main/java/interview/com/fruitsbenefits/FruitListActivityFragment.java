package interview.com.fruitsbenefits;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class FruitListActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    ArrayList<FruitDetails> data;
    FruitListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    public static final String TAG = "FruitListFragment";
    public static final String [] PROJECTION = new String[] {
            FruitBenefitsProvider.ID,
            FruitBenefitsProvider.NAME,
            FruitBenefitsProvider.DESC
    };
    public FruitListActivityFragment() {
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        setRetainInstance(true);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        View v = inflater.inflate(R.layout.fragment_fruit_list, container, false);
        final ExpandableListView eListView = (ExpandableListView)v.findViewById(R.id.list);
        eListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                boolean enable = false;
                if(eListView != null && eListView.getChildCount() > 0){
                    boolean firstItemVisible = eListView.getFirstVisiblePosition() == 0;
                    boolean topOfFirstItemVisible = eListView.getChildAt(0).getTop() == 0;
                    enable = firstItemVisible && topOfFirstItemVisible;
                }
                if(mSwipeRefreshLayout != null)
                    mSwipeRefreshLayout.setEnabled(enable);
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh_layout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "refresh called");
                new ContentUpdateTask().execute();
            }
        });
        data = new ArrayList<>();
        mAdapter = new FruitListAdapter();
        eListView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.d(TAG, "onCreateLoader");
        CursorLoader cl = new CursorLoader(getActivity(), FruitBenefitsProvider.CONTENT_URI,
                PROJECTION, null, null, null);
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished");
        if(data != null && !cursor.isClosed()) {
            Log.d(TAG, "Data count = " + cursor.getCount());
            data.clear();
            if(cursor.moveToFirst()) {
                do {
                    FruitDetails fd = new FruitDetails();
                    fd.setName(cursor.getString(cursor.getColumnIndex(FruitBenefitsProvider.NAME)));
                    fd.setDetails(cursor.getString(cursor.getColumnIndex(FruitBenefitsProvider.DESC)));
                    data.add(fd);
                } while(cursor.moveToNext());
            }
        }
        Log.d(TAG, "notify data set changed");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class FruitListAdapter extends BaseExpandableListAdapter {
        @Override
        public int getGroupCount() {
            if(data == null)
                return 0;
            return  data.size();
        }

        @Override
        public int getChildrenCount(int i) {
            if(data == null)
                return 0;
            return 1;
        }

        @Override
        public Object getGroup(int i) {
            Log.d(TAG, "group id: "+i);
            if(data == null)
                return null;
            return data.get(i).getName();
        }

        @Override
        public Object getChild(int i, int i1) {
            Log.d(TAG, "group id "+i+"child id "+i1);
            if(data == null)
                return 0;
            return data.get(i).getDetails();
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(FruitListActivityFragment.this.getActivity());
            tv.setText(data.get(i).getName());
            tv.setTextSize(20);
            tv.setPadding(16, 20, 5, 20);
            tv.setTextColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
            return tv;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            TextView tv = new TextView(FruitListActivityFragment.this.getActivity());
            tv.setPadding(16, 5, 5, 5);
            tv.setText(data.get(i).getDetails());
            return tv;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }
    }

    private class ContentUpdateTask extends AsyncTask<String, Void, String> {

        public static final String urlString = "https://api.myjson.com/bins/3maj0";

        @Override
        protected String doInBackground(String... strings) {
            InputStream isr = null;
            int size = 30000;
            Log.d(TAG, "doInBackground()"+urlString);
            try {
                URL url = new URL(urlString);
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
                Cursor cr = getActivity().getContentResolver().query(
                        FruitBenefitsProvider.CONTENT_URI, PROJECTION, null, null, null);
                if(cr != null && cr.getCount() == array.length()) {
                    Log.d(TAG, "No new update");
                } else if (cr.getCount() < array.length()){
                    cr.moveToFirst();
                    for(int i= cr.getCount(); i < array.length(); i++ ) {
                       JSONObject obj = array.getJSONObject(i);
                       ContentValues cv = new ContentValues();
                       String name = obj.getString("name");
                       cv.put(FruitBenefitsProvider.NAME, name);
                       String description = obj.getString("description");
                       cv.put(FruitBenefitsProvider.DESC, description);
                       getActivity().getContentResolver().insert(FruitBenefitsProvider.CONTENT_URI,
                               cv);
                    }
                }
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
            if(mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getActivity(), R.string.no_update, Toast.LENGTH_SHORT).show();
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
}
