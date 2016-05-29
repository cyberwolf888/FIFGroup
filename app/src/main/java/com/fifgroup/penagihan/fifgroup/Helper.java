package com.fifgroup.penagihan.fifgroup;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Created by Master on 5/23/2016.
 */
public class Helper extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Name = "nama_kolektor";
    public static final String ID = "id_kolektor";

    // JSON Node names
    public static final String TAG_LISTITEM = "databeban";
    public static final String TAG_NO_KONTRAK = "no_kontrak";
    public static final String TAG_NAMA = "nama";
    public static final String TAG_ALAMAT = "tmpat_tagih";
    public static final String TAG_PHONE = "tlp";
    public static final String TAG_LABEL_STATUS = "label_status";
    public static final String TAG_STATUS = "sts_laporan";
    public static final String TAG_HUTANG_POKOK = "pokok_hutang";
    public static final String TAG_TELAH_BAYAR = "telah_bayar";
    public static final String TAG_ANGSURAN = "angsuran";

    SharedPreferences sharedpreferences;
    public Helper(Context mContext){
        sharedpreferences = mContext.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public boolean validateLogin(){

        if(sharedpreferences.contains(Name)){
            return true;
        }
        return false;
    }

    public String getUserID(){
        String id=sharedpreferences.getString(ID,"");

        return id;
    }

    public String getUserName(){
        String name=sharedpreferences.getString(Name,"");

        return name;
    }

    public String formatNumber(Integer number){
        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("Rp. ");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        kursIndonesia.setDecimalFormatSymbols(formatRp);
        kursIndonesia.setMaximumFractionDigits(0);

        return kursIndonesia.format(number);
    }

    public void logout(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }
}
