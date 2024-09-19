package com.boscloner.app.boscloner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import java.util.UUID;

/**
 * Created by jpiat on 10/15/15.
 */
public interface IBluetoothManager {


    public abstract void setContext(Context context);
    public abstract void setDevice(BluetoothDevice device);
    public abstract BluetoothDevice getDevice();

    public abstract boolean getServices(IUpdatedDescriptionCallBack callBack);
    public abstract boolean connect(UUID rxUuid, UUID txUuid);
    public abstract boolean disconnect();
    public abstract byte[] read(UUID serviceUuid);
    public abstract boolean enableAsyncRead(UUID serviceUuid, IDataReceivedCallBack callBack);
    public abstract boolean disableAsyncRead(UUID serviceUuid);
    public abstract boolean write(UUID serviceUuid, byte [] data, int synchronous);
    public abstract String getName();
    public abstract boolean isConnected();

}
