package com.b2b.home.server;

/**
 * Created by Home on 23-01-2017.
 */

public class Transaction {
    private String id;
    private String amount;
    private String tramsfertype;
    private String number;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTramsfertype() {
        return tramsfertype;
    }

    public void setTramsfertype(String tramsfertype) {
        this.tramsfertype = tramsfertype;
    }
}
