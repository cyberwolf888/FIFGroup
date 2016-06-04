package com.fifgroup.penagihan.fifgroup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainMenu extends AppCompatActivity {

    private View mProgressView;
    private View mContentLayoutView;

    TextView ketText;
    ListView listView;
    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new Helper(MainMenu.this);
        if(!helper.validateLogin()){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContentLayoutView = findViewById(R.id.main_content_layout);
        mProgressView = findViewById(R.id.main_progress);

        ketText = (TextView) findViewById(R.id.ketText);
        listView = (ListView) findViewById(R.id.mobile_list);

        //get request
        if(isNetworkAvailable()){
            showProgress(true);
            new GetList().execute();
        }else{
            ketText.setText("Tidak dapat terhubung dengan server!");
            ketText.setVisibility(View.VISIBLE);
            Toast.makeText(getApplicationContext(), "Koneksi internet tidak ditemukan!", Toast.LENGTH_LONG).show();
        }

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mContentLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
            mContentLayoutView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mContentLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mContentLayoutView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    //JSON handler
    private class GetList extends AsyncTask<Void, Void, Void> {

        // Hashmap for ListView
        ArrayList<HashMap<String, String>> itemList;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            /*
            pDialog = new ProgressDialog(MainMenu.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            */
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // Creating service handler class instance
            WebRequest webreq = new WebRequest();
            HashMap<String, String> data = new HashMap<String,String>();
            data.put("id_kolektor",helper.getUserID());

            // Making a request to url and getting response
            String jsonStr = webreq.sendPostRequest("databeban.php", data);

            //Log.d("Response: ", "> " + jsonStr);

            itemList = ParseJSON(jsonStr);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            /*
            if (pDialog.isShowing())
                pDialog.dismiss();
            */
            /**
             * Updating parsed JSON data into ListView
             * */
            if(itemList != null){
                ListAdapter adapter = new SimpleAdapter(
                        MainMenu.this,
                        itemList,
                        R.layout.list_item,
                        new String[]{helper.TAG_NO_KONTRAK, helper.TAG_NAMA, helper.TAG_ALAMAT, helper.TAG_PHONE, helper.TAG_LABEL_STATUS},
                        new int[]{R.id.no_kontrak, R.id.nama, R.id.alamat, R.id.phone, R.id.lbl_status}
                ){
                    @Override
                    public View getView (int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        HashMap<String, String> data = itemList.get(position);
                        View list_row = (View) view.findViewById(R.id.list_lay);
                        TextView lbl_status = (TextView) view.findViewById(R.id.lbl_status);

                        if(data.get(helper.TAG_LABEL_STATUS).equals("Bayar")){
                            lbl_status.setTextColor(Color.BLUE);
                        }else if (data.get(helper.TAG_LABEL_STATUS).equals("Lunas")){
                            lbl_status.setTextColor(Color.GREEN);
                        }else{
                            lbl_status.setTextColor(Color.RED);
                        }
                        return view;
                    }
                };

                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                        //Log.d("Cicked: ", "> " + position);

                        HashMap<String, String> selected = itemList.get(position);

                        //Log.d("Selected :","> " + selected);
                        if(selected.get(helper.TAG_LABEL_STATUS).equals("Bayar") || selected.get(helper.TAG_LABEL_STATUS).equals("Lunas")){
                            Intent intent = new Intent(getApplicationContext(), Detail.class);
                            intent.putExtra("RowData", selected);
                            startActivity(intent);
                            finish();
                        }else{
                            Intent intent = new Intent(getApplicationContext(), Penagihan.class);
                            intent.putExtra("RowData", selected);
                            startActivity(intent);
                            finish();
                        }

                    }
                });
            }else{
                ketText.setText("Tidak ditemukan data customer.");
                ketText.setVisibility(View.VISIBLE);
            }

            showProgress(false);
        }

    }

    private ArrayList<HashMap<String, String>> ParseJSON(String json) {
        if (json != null) {
            try {
                // Hashmap for ListView
                ArrayList<HashMap<String, String>> itemList = new ArrayList<HashMap<String, String>>();

                JSONObject jsonObj = new JSONObject(json);

                // Getting JSON Array node
                JSONArray listitem = jsonObj.getJSONArray(helper.TAG_LISTITEM);

                // looping through All Students
                for (int i = 0; i < listitem.length(); i++) {
                    JSONObject c = listitem.getJSONObject(i);

                    String id_cst = c.getString(helper.TAG_ID_CST);
                    String no_kontrak = c.getString(helper.TAG_NO_KONTRAK);
                    String nama = c.getString(helper.TAG_NAMA);
                    String alamat = c.getString(helper.TAG_ALAMAT);
                    String phone = c.getString(helper.TAG_PHONE);
                    String lbl_status = c.getString(helper.TAG_LABEL_STATUS);
                    String status = c.getString(helper.TAG_STATUS);
                    String pokok_hutang = c.getString(helper.TAG_HUTANG_POKOK);
                    String telah_bayar = c.getString(helper.TAG_TELAH_BAYAR);
                    String angsuran = c.getString(helper.TAG_ANGSURAN);
                    String jatuh_tempo = c.getString(helper.TAG_JATUH_TEMPO);

                    // tmp hashmap for single student
                    HashMap<String, String> dataList = new HashMap<String, String>();

                    // adding each child node to HashMap key => value
                    dataList.put(helper.TAG_ID_CST, id_cst);
                    dataList.put(helper.TAG_NO_KONTRAK, no_kontrak);
                    dataList.put(helper.TAG_NAMA, nama);
                    dataList.put(helper.TAG_ALAMAT, alamat);
                    dataList.put(helper.TAG_PHONE, phone);
                    dataList.put(helper.TAG_LABEL_STATUS, lbl_status);
                    dataList.put(helper.TAG_STATUS, status);
                    dataList.put(helper.TAG_HUTANG_POKOK, pokok_hutang);
                    dataList.put(helper.TAG_TELAH_BAYAR, telah_bayar);
                    dataList.put(helper.TAG_ANGSURAN, angsuran);
                    dataList.put(helper.TAG_JATUH_TEMPO, jatuh_tempo);

                    // adding student to students list
                    itemList.add(dataList);
                }
                return itemList;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Log.e("ServiceHandler", "Couldn't get any data from the url");
            return null;
        }
    }

    //toolbar menu
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent intent = new Intent(getApplicationContext(), About.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.action_home) {
            Intent intent = new Intent(getApplicationContext(), MainMenu.class);
            startActivity(intent);
            finish();
        }
        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Logout")
                    .setMessage("Apakah anda yakin untuk logout dari aplikasi ini?")
                    .setPositiveButton("Ya", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            helper.logout();

                            Intent intent = new Intent(getApplicationContext(), Login.class);
                            startActivity(intent);
                            finish();
                        }

                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

}
