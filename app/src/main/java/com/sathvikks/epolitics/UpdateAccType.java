package com.sathvikks.epolitics;

public class UpdateAccType {
    private myListener listener;

    public void setListener(myListener listener) {
        this.listener = listener;
    }

    public void fetchAccType(String accType) {
        listener.onAccTypeUpdate(accType);
    }
}
