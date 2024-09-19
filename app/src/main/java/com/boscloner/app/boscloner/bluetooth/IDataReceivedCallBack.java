package com.boscloner.app.boscloner.bluetooth;

/**
 * Created by jpiat on 10/15/15.
 */
public interface IDataReceivedCallBack {

    public void onDataRead(byte [] data);
    public void onDisconnect();

}
