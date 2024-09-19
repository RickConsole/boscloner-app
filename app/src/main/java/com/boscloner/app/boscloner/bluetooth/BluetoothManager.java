package com.boscloner.app.boscloner.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by jpiat on 10/15/15.
 */
public class BluetoothManager implements IBluetoothManager {

    private Context manager_context ;
    private BluetoothDevice manager_device ;
    private IUpdatedDescriptionCallBack device_update_callback ;


    private BluetoothSocket rx_socket ;
    private BluetoothSocket tx_socket ;

    private boolean asyncReadEnabled ;

    public BluetoothManager(Context context, BluetoothDevice device){
        this.manager_context = context ;
        this.manager_device = device ;
    }
    public  BluetoothDevice getDevice(){
        return this.manager_device;
    }

    public String getName(){
        return this.manager_device.getName();
    }
    @Override
    public void setContext(Context context) {
        this.manager_context = context ;
    }

    @Override
    public void setDevice(BluetoothDevice device) {
        this.manager_device = device ;
    }


    public ParcelUuid[] alternatGetDeviceUuids(BluetoothDevice device) {
        ArrayList<ParcelUuid> result = new ArrayList<ParcelUuid>();

        try {
            Method method = device.getClass().getMethod("getUuids", null);
            ParcelUuid[] phoneUuids = (ParcelUuid[]) method.invoke(device, null);
            if (phoneUuids != null) {
                for (ParcelUuid uuid : phoneUuids) {
                    Log.d(BluetoothManager.class.getName(), device.getName() + ": " + uuid.toString());
                    result.add(uuid);
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
           Log.e(BluetoothManager.class.getName(), "getDeviceUuids() failed", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.e(BluetoothManager.class.getName(), "getDeviceUuids() failed", e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.e(BluetoothManager.class.getName(), "getDeviceUuids() failed", e);
        }

        return (ParcelUuid[]) result.toArray();
    }

    @Override
    public boolean getServices(IUpdatedDescriptionCallBack callBack) {
        this.device_update_callback = callBack ;
        ParcelUuid [] uuids = this.manager_device.getUuids();
        //if(uuids == null) uuids = this.alternatGetDeviceUuids(this.manager_device);
        if(uuids != null) {
            Thread thread = new Thread(){
                public void run(){
                    List<UUID> services = new ArrayList<UUID>();
                    for (ParcelUuid p : BluetoothManager.this.manager_device.getUuids()) {
                        services.add(p.getUuid());
                    }
                    BluetoothManager.this.device_update_callback.onUpdateDeviceDescription(BluetoothManager.this, services);
                }
            };
            thread.start();
            return true;
        }else{
            return false ;
        }
    }

    public boolean disconnect(){
        if(this.tx_socket != null ){
            try{
            this.tx_socket.close();
            }catch(Exception e){

            }
            this.tx_socket = null ;
        }
        if(this.rx_socket != null){
            try{
                this.rx_socket.close();
            }catch(Exception e){

            }
            this.rx_socket = null ;
        }
        return true ;
    }
    @Override
    public boolean connect(UUID rxUuid, UUID txUuid ) {
        try {
            if(rxUuid == null && txUuid == null) return false ;
            //this.bluetooth_device.setPin(this.getPin());
            if(rxUuid != null) {
                this.rx_socket = this.manager_device.createRfcommSocketToServiceRecord(rxUuid); //TODO: check if one of the device UUID matches SPP.
            }else{
                this.rx_socket = null ;
            }

            if(txUuid.equals(rxUuid)){
                tx_socket = rx_socket ;
            }else if(txUuid != null) {
                this.tx_socket = this.manager_device.createRfcommSocketToServiceRecord(txUuid); //TODO: check if one of the device UUID matches SPP.
            }else{
                this.tx_socket = null ;
            }
        } catch (IOException e) {
            Log.e(BluetoothManager.class.toString(), e.getMessage());
            return false ;
        }

        try {
            if(rx_socket != null) this.rx_socket.connect();
            if(tx_socket != null && !tx_socket.equals(rx_socket)) this.tx_socket.connect();
        } catch (IOException e) {
            Log.e(BluetoothManager.class.toString(), e.getMessage());
            return false ;
        }
        if((this.rx_socket.isConnected() || rx_socket == null)&& (this.tx_socket.isConnected() || tx_socket == null)) {
           return true ;
        }else{
            return false ;
        }
    }

    public boolean isConnected(){
        if((rx_socket != null && this.rx_socket.isConnected())&& (tx_socket != null && this.tx_socket.isConnected())) {
            return true ;
        }
        return false ;
    }

    @Override
    public byte[] read(UUID serviceUuid) {
        byte [] data_array = new byte[256];
        if(rx_socket == null || !rx_socket.isConnected()) return null ;
       try {
           this.rx_socket.getInputStream().read(data_array);
           return data_array ;
       }catch(Exception e){
           Log.e(BluetoothManager.class.toString(), e.getMessage());
           return null ;
       }
    }

    @Override
    public boolean enableAsyncRead(UUID serviceUuid, final IDataReceivedCallBack callBack) {
        try {
            if (this.rx_socket == null || this.rx_socket.getInputStream().available() < 0) {
                return false;
            }else{
                asyncReadEnabled =  true ;
                Thread t = new Thread(){
                    public void run() {
                        byte [] data_array = new byte[256];
                        while(asyncReadEnabled && rx_socket != null &&rx_socket.isConnected()) {
                            try {
                                int nbRead = BluetoothManager.this.rx_socket.getInputStream().read(data_array);
                                if(nbRead < 0){
                                    //TODO: notify observers of disconnect ...
                                    callBack.onDisconnect();
                                    break ;
                                }
                                byte [] reduced = Arrays.copyOfRange(data_array, 0, nbRead);
                                Log.e(this.getClass().getName(), new String(reduced));
                                callBack.onDataRead(reduced);
                                Thread.sleep(1);
                            } catch (Exception e) {
                                callBack.onDisconnect();
                                Log.e(BluetoothManager.class.toString(), e.getMessage());
                                break ;
                            }

                        }
                    }
                };
                t.start();
                return true ;
            }
        }catch(Exception e ){
            Log.e(this.getClass().getName(), e.getMessage());
            return false ;
        }
    }

    @Override
    public boolean disableAsyncRead(UUID serviceUuid) {
        this.asyncReadEnabled = false ;
        return true ;
    }

    @Override
    public boolean write(UUID serviceUuid, byte[] data, int synchronous) {
        try {
            if(tx_socket == null || !tx_socket.isConnected()) return false ;
            this.tx_socket.getOutputStream().write(data);
            this.tx_socket.getOutputStream().flush();
            return true ;
        }catch(Exception e){
            return false ;
        }
    }
}
