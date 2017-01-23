package com.b2b.home.axisserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class HookStateReceiver extends BroadcastReceiver {
    public HookStateReceiver() {
    }
    public static boolean CallState=false;
    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.i("Broadcast", "Received");
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        //Log.i("Number","asdas");
        telephonyManager.listen(new PhoneStateListener(){

            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                if(state==TelephonyManager.CALL_STATE_OFFHOOK){


                Intent i=new Intent(context,DTMFRecorder.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if(incomingNumber!=null)
                        if (!incomingNumber.equals("")) {
                            Log.i("Broadcast", "Phone state: off hook - in call"+incomingNumber);
                            i.putExtra("number", incomingNumber);
                            CallState=true;
                            context.startService(i);
                        }
                }
                if (state!=TelephonyManager.CALL_STATE_OFFHOOK && CallState==true){
                    CallState=false;
                    Log.i("Broadcast", "Call Cut panniyachu :)");

                }
            }
        },PhoneStateListener.LISTEN_CALL_STATE);
    }
}
