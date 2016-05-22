package com.fifgroup.penagihan.fifgroup;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Master on 5/23/2016.
 */
public class Helper extends AppCompatActivity {
    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Name = "nama_kolektor";
    public static final String ID = "id_kolektor";

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

    public void logout(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }
}
