package com.b2b.home.axisserver;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Home on 23-01-2017.
 */

public class Pay extends AsyncTask<Transaction,Void,Integer> {

    @Override
    protected Integer doInBackground(Transaction... params) {
        Log.i("Transaction"," Account "+params[0].getAcc()+" Amount"+params[0].getAmount()+" IFSC "+params[0].getIfsc()+" Number"+params[0].getNumber());

        HttpURLConnection connection = null;
        String s = "";

        String surl=ServerDetails.BaseURL+"addTransaction?number="+params[0].getNumber()+"&amount="+params[0].getAmount()
                +"&IFSC="+params[0].getIfsc()+"&acc="+params[0].getAcc();
        URL url = null;
        try {
            url = new URL(surl);

            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream input = connection.getInputStream();
            char c;
            while ((c = (char) input.read()) != (char) -1)
                s += c;

            // Log.i("Server return",s);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(s.equals("true"))
        return 1;
        else
            return 0;
    }

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);

        Log.i("Post Update","Send Acknowlwdgement");
    }
}
