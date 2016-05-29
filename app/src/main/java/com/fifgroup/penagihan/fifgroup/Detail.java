package com.fifgroup.penagihan.fifgroup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;

import java.util.HashMap;

public class Detail extends AppCompatActivity {

    HashMap<String, String> RowData;

    private View mProgressView;
    private View mDetailView;

    TextView dtNoKontrak;
    TextView dtNama;
    TextView dtAlamat;
    TextView dtNomerHp;
    TextView dtHutang;
    TextView dtTelahBayar;
    TextView dtStatus;

    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new Helper(Detail.this);
        if(!helper.validateLogin()){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        RowData = (HashMap<String, String>)intent.getSerializableExtra("RowData");
        //Log.d("RowData :","> " + RowData);

        dtNoKontrak = (TextView) findViewById(R.id.dtNoKontrak);
        dtNama = (TextView) findViewById(R.id.dtNama);
        dtAlamat = (TextView) findViewById(R.id.dtAlamat);
        dtNomerHp = (TextView) findViewById(R.id.dtNomerHp);
        dtHutang = (TextView) findViewById(R.id.dtHutang);
        dtTelahBayar = (TextView) findViewById(R.id.dtTelahBayar);
        dtStatus = (TextView) findViewById(R.id.dtStatus);

        mDetailView = findViewById(R.id.detail_view);
        mProgressView = findViewById(R.id.detail_progress);

        if(isNetworkAvailable()){
            showProgress(true);
            new GetDetail().execute();
        }else{
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

            mDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
            mDetailView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mDetailView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private class GetDetail extends AsyncTask<Void, Void, Void> {

        private String jsonStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            WebRequest webreq = new WebRequest();
            HashMap<String, String> data = new HashMap<String,String>();
            data.put("id_kolektor",helper.getUserID());
            data.put("no_kontrak",RowData.get(helper.TAG_NO_KONTRAK));

            jsonStr = webreq.sendPostRequest("detail.php", data);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //showProgress(false);
            //Log.d("Response: ", "> " + jsonStr);

            if(jsonStr != null && !jsonStr.isEmpty()){
                try{
                    JSONObject jObj = new JSONObject(jsonStr);
                    //Log.d("jObj: ", "> " + jObj);

                    dtNoKontrak.setText(jObj.getString(helper.TAG_NO_KONTRAK));
                    dtNama.setText(jObj.getString(helper.TAG_NAMA));
                    dtAlamat.setText(jObj.getString(helper.TAG_ALAMAT));
                    dtNomerHp.setText(jObj.getString(helper.TAG_PHONE));
                    dtHutang.setText(helper.formatNumber(Integer.valueOf(jObj.getString(helper.TAG_HUTANG_POKOK))));
                    dtTelahBayar.setText(helper.formatNumber(Integer.valueOf(jObj.getString(helper.TAG_TELAH_BAYAR))));
                    dtStatus.setText(jObj.getString(helper.TAG_LABEL_STATUS));

                    if(jObj.get(helper.TAG_STATUS).equals("1")){
                        dtStatus.setTextColor(Color.BLUE);
                    }else if((jObj.get(helper.TAG_STATUS).equals("2"))){
                        dtStatus.setTextColor(Color.GREEN);
                    }else{
                        dtStatus.setTextColor(Color.RED);
                    }

                }catch (Exception e) {
                    Log.e(Login.class.getSimpleName(), e.toString());
                }
            }else{
                Toast.makeText(getApplicationContext(), "Gagal menyambung ke server, silahkan coba lagi.", Toast.LENGTH_LONG).show();
            }
            showProgress(false);
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
