package com.b2b.home.axisserver;

/**
 * Created by Home on 23-01-2017.
 */

public class Decoder {

    public static Transaction decode(String data){
        Transaction t=new Transaction();
        String[] d=data.split("\\*");
        t.setAmount(d[0]);
        t.setAcc(d[1]);
        t.setIfsc(d[2]);
        return t;

    }
}
