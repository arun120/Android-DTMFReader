package com.b2b.home.server;

/**
 * Created by Home on 23-01-2017.
 */

public class Decoder {

    public static Transaction decode(String data){
        Transaction t=new Transaction();
        String[] d=data.split("\\*");
        t.setId(d[0]);
        t.setAmount(d[1]);
        if(d[2].equals("0"))
            t.setTramsfertype("NEFT");
        else
        t.setTramsfertype("RTGS");
        return t;

    }
}
