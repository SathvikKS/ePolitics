package com.sathvikks.epolitics;

public class UpdateUserRegion {
    private myListener listener;

    public void setListener(myListener listener) {
        this.listener = listener;
    }

    public void fetchRegion(String region) {
        listener.onAccRegionUpdate(region);
    }
}

