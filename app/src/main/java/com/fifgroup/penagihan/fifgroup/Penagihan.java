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
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class Penagihan extends AppCompatActivity {

    private TagihTask mTagihTask = null;

    TextView txtNoKontrak;
    TextView txtNama;
    TextView txtAlamat;
    TextView txtPhone;
    TextView txtHutangPokok;
    TextView txtTelahBayar;
    TextView txtJatuhTempo;
    TextView txtAngsuran;
    TextView txtStatus;
    TextView txtDenda;
    EditText txt_bayar;
    EditText txt_angsuran;

    HashMap<String, String> RowData;

    private View mProgressView;
    private View mPenagihanForm;

    Helper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        helper = new Helper(Penagihan.this);
        if(!helper.validateLogin()){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_penagihan);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        RowData = (HashMap<String, String>)intent.getSerializableExtra("RowData");
        Log.d("RowData :","> " + RowData);

        txtNoKontrak = (TextView) findViewById(R.id.txtNoKontrak);
        txtNama = (TextView) findViewById(R.id.txtNama);
        txtAlamat = (TextView) findViewById(R.id.txtAlamat);
        txtPhone = (TextView) findViewById(R.id.txtNomerHp);
        txtHutangPokok = (TextView) findViewById(R.id.txtHutang);
        txtTelahBayar = (TextView) findViewById(R.id.txtTelahBayar);
        txtJatuhTempo = (TextView) findViewById(R.id.txtJatuhTempo);
        txtAngsuran = (TextView) findViewById(R.id.txtAngsuran);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtDenda = (TextView) findViewById(R.id.txtDenda);
        txt_bayar = (EditText) findViewById(R.id.txt_bayar);
        txt_angsuran = (EditText) findViewById(R.id.txt_angsuran);

        txtNoKontrak.setText(RowData.get(helper.TAG_NO_KONTRAK));
        txtNama.setText(RowData.get(helper.TAG_NAMA));
        txtAlamat.setText(RowData.get(helper.TAG_ALAMAT));
        txtPhone.setText(RowData.get(helper.TAG_PHONE));
        txtHutangPokok.setText(helper.formatNumber(Integer.valueOf(RowData.get(helper.TAG_HUTANG_POKOK))));
        txtTelahBayar.setText(helper.formatNumber(Integer.valueOf(RowData.get(helper.TAG_TELAH_BAYAR))));
        txtJatuhTempo.setText(RowData.get(helper.TAG_JATUH_TEMPO));
        txtAngsuran.setText(helper.formatNumber(Integer.valueOf(RowData.get(helper.TAG_ANGSURAN))));
        txtDenda.setText(helper.formatNumber(getDenda(RowData.get(helper.TAG_TELAH_BAYAR), RowData.get(helper.TAG_HUTANG_POKOK), RowData.get(helper.TAG_JATUH_TEMPO))));
        txtStatus.setText(RowData.get(helper.TAG_LABEL_STATUS));

        if(RowData.get(helper.TAG_STATUS).equals("1")){
            txtStatus.setTextColor(Color.BLUE);
        }else if((RowData.get(helper.TAG_STATUS).equals("2"))){
            txtStatus.setTextColor(Color.GREEN);
        }else{
            txtStatus.setTextColor(Color.RED);
        }

        mPenagihanForm = findViewById(R.id.penagihan_form);
        mProgressView = findViewById(R.id.penagihan_progress);

        Button tagih = (Button) findViewById(R.id.btn_tagih);
        tagih.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tagih();
            }
        });
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

            mPenagihanForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mPenagihanForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mPenagihanForm.setVisibility(show ? View.GONE : View.VISIBLE);
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
            mPenagihanForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private int getDenda(String telah_bayar, String hutang_pokok, String jatuh_tempo){
        int denda = 0;
        try{
            int sisa_hutang = Integer.valueOf(hutang_pokok) - Integer.valueOf(telah_bayar);
            Log.d("Sisa Hutang :","> "+ sisa_hutang);
            Log.d("Telah Bayar :","> "+ telah_bayar);
            Log.d("Hutang Pokok :","> "+ hutang_pokok);
            SimpleDateFormat patterm = new SimpleDateFormat("MM/dd/yyyy");
            Date strDate = patterm.parse(jatuh_tempo);
            Date currentDate = new Date(); //patterm.parse("07/05/2016");
            if (currentDate.after(strDate)) {
                denda = sisa_hutang * 5/1000;
            }
        }catch (Exception e){

        }
        return denda;
    }

    private void tagih(){
        txt_bayar.setError(null);
        txt_angsuran.setError(null);

        String bayar = txt_bayar.getText().toString();
        String angsuran = txt_angsuran.getText().toString();
        int denda = getDenda(RowData.get(helper.TAG_TELAH_BAYAR), RowData.get(helper.TAG_HUTANG_POKOK), RowData.get(helper.TAG_JATUH_TEMPO));
        int m_angsuran = Integer.valueOf(RowData.get(helper.TAG_ANGSURAN));
        int minimal_bayar = denda+m_angsuran;
        Log.d("Response: ", "> " + minimal_bayar);

        boolean cancel = false;
        View focusView = null;

        if (!isBayarValid(bayar)) {
            txt_bayar.setError("Karakter terlalu pendek!");
            focusView = txt_bayar;
            cancel = true;
        }else if (minimal_bayar!=Integer.parseInt(bayar)){
            txt_bayar.setError("Pembayaran harus "+helper.formatNumber(minimal_bayar));
            focusView = txt_bayar;
            cancel = true;
        } else if(TextUtils.isEmpty(angsuran)){
            txt_angsuran.setError("Angsuran tidak boleh kosong!");
            focusView = txt_angsuran;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            if(isNetworkAvailable()){
                showProgress(true);
                mTagihTask = new TagihTask(bayar, angsuran);
                mTagihTask.execute((Void) null);
            }else{
                Toast.makeText(getApplicationContext(), "Koneksi internet tidak ditemukan!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isBayarValid(String password) {
        return password.length() > 4;
    }

    public class TagihTask extends AsyncTask<Void, Void, Void> {

        private final String mBayar;
        private final String mAngsuran;
        private String jsonStr;

        TagihTask(String bayar, String angsuran) {
            mBayar = bayar;
            mAngsuran = angsuran;
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            WebRequest webreq = new WebRequest();
            HashMap<String, String> data = new HashMap<String,String>();
            data.put("id_kolektor",helper.getUserID());
            //data.put("no_kontrak",RowData.get(helper.TAG_NO_KONTRAK));
            data.put("id_cst",RowData.get(helper.TAG_ID_CST));
            data.put("bayar",mBayar);
            data.put("angsuran",mAngsuran);
            data.put("denda",Integer.toString(getDenda(RowData.get(helper.TAG_TELAH_BAYAR), RowData.get(helper.TAG_HUTANG_POKOK), RowData.get(helper.TAG_JATUH_TEMPO))));
            Log.d("Data: ", "> " + data);
            jsonStr = webreq.sendPostRequest("penagihan.php", data);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mTagihTask = null;
            showProgress(false);
            Log.d("Response: ", "> " + jsonStr);
            if(jsonStr != null && !jsonStr.isEmpty()){
                try{
                    JSONObject jObj = new JSONObject(jsonStr);
                    String status =  jObj.getString("status");
                    if(status.equals("1")){
                        Intent intent = new Intent(getApplicationContext(), Detail.class);
                        intent.putExtra("RowData", RowData);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(), "Gagal menyimpan data pada server!", Toast.LENGTH_LONG).show();
                    }
                }catch (Exception e) {
                    Log.e(Login.class.getSimpleName(), e.toString());
                }
            }else{
                Toast.makeText(getApplicationContext(), "Gagal menyambung ke server, silahkan coba lagi.", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            mTagihTask = null;
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
