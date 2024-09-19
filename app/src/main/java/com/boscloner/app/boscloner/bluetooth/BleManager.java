package com.boscloner.app.boscloner.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jpiat on 10/15/15.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BleManager  extends BluetoothGattCallback implements IBluetoothManager {
    private Context manager_context ;
    private BluetoothDevice manager_device ;
    private IUpdatedDescriptionCallBack device_update_callback ;
    private ManagerState my_state ;
    private List<UUID> services ;
    private BluetoothGatt gatt ;
    private Map<UUID, IDataReceivedCallBack> readUUIDs;
    private UUID writeUUID;
    private int rssi ;

    private enum ManagerState {
        IDLE,
        READING,
        WRITING,
        SERVICE_DISCOVERY,
        GET_RSSI
    };


    public BleManager(Context context, BluetoothDevice device){
        this.manager_context = context ;
        this.manager_device = device ;
        this.services = new ArrayList<UUID>();
        my_state = ManagerState.IDLE ;
        readUUIDs = new HashMap<UUID, IDataReceivedCallBack>();
    }

    public String getName(){
        return this.manager_device.getName();
    }

    public  BluetoothDevice getDevice(){
        return this.manager_device;
    }

    public boolean getRssi(IUpdatedDescriptionCallBack callBack){
        if(my_state != ManagerState.IDLE || this.gatt == null) return false ;
        my_state = ManagerState.GET_RSSI ;
        this.device_update_callback = callBack;
        return this.gatt.readRemoteRssi();

    }

    @Override
    public void setContext(Context context) {
        this.manager_context = context ;
    }

    @Override
    public void setDevice(BluetoothDevice device) {
        this.manager_device = device ;
    }

    @Override
    public boolean getServices(IUpdatedDescriptionCallBack callBack) {
        if(my_state != ManagerState.IDLE) return false ;
        my_state = ManagerState.SERVICE_DISCOVERY ;
        this.device_update_callback = callBack;
        if(this.services.size() == 0 ) {
            gatt = this.manager_device.connectGatt(this.manager_context, false, this);
            if(gatt == null) return false ;
        }else{
            my_state = ManagerState.IDLE ;
            Thread thread = new Thread(){
                public void run(){
                    if(BleManager.this.device_update_callback != null) {
                        BleManager.this.device_update_callback.onUpdateDeviceDescription(BleManager.this, BleManager.this.services);
                    }
                }
            };
            thread.start();
        }
        return true ;
    }

    private boolean refreshServices(IUpdatedDescriptionCallBack callBack) {
        if(my_state != ManagerState.IDLE) return false ;
        my_state = ManagerState.SERVICE_DISCOVERY ;
        this.device_update_callback = callBack;
        this.services.clear();
        if(gatt == null) {
            gatt = this.manager_device.connectGatt(this.manager_context, false, this);
        }else{
            gatt.discoverServices();
        }
        if(gatt == null) return false ;
        return true ;
    }

    public boolean isConnected(){
        if(this.gatt != null ) return true ;
        return false ;
    }

    public boolean disconnect() {
        if(this.gatt != null) {
            this.gatt.disconnect();
        }
        this.gatt = null ;
        return true ;
    }

    @Override
    public boolean connect(UUID rxUuid, UUID txUuid) {
        if(this.gatt == null) {
            this.gatt = this.manager_device.connectGatt(this.manager_context, false, this);
        }
        if(gatt != null){
            return true ;
        }
        return false ;
    }

    @Override
    public byte[] read(UUID serviceUuid) {
        if(this.gatt.getServices().size() == 0){
            this.refreshServices(null);
            while(my_state == ManagerState.SERVICE_DISCOVERY){
                try {
                    Thread.sleep(1);
                }catch(Exception e){

                }
            }
        }

        BluetoothGattCharacteristic c_to_read = null ;
        for(BluetoothGattService s : this.gatt.getServices()){
            for(UUID know_service : this.services){
                if(s.getUuid().equals(know_service)){
                    for(BluetoothGattCharacteristic c : s.getCharacteristics()){
                        if(c.getUuid().equals(serviceUuid)){
                            c_to_read = c ;
                            this.my_state = ManagerState.READING ;
                            if(!this.gatt.readCharacteristic(c)){
                                this.my_state = ManagerState.IDLE ;
                                return null ;
                            }else{
                                break ;
                            }
                        }
                    }
                    if(this.my_state == ManagerState.READING) break ;
                }
                if(this.my_state == ManagerState.READING) break ;
            }
            if(this.my_state == ManagerState.READING) break ;
        }
        while(this.my_state != ManagerState.IDLE){
            try{
            Thread.sleep(1);
            }catch(Exception e){
            }
        }
        if(c_to_read != null) {
            return c_to_read.getValue();
        }else{
            return null ;
        }

    }

    @Override
    public boolean disableAsyncRead(UUID serviceUuid) {
        readUUIDs.remove(serviceUuid);
        return true ;
    }

    @Override
    public boolean enableAsyncRead(UUID serviceUuid, IDataReceivedCallBack callBack) {

        if(this.my_state != ManagerState.IDLE) return false ;
        if(this.gatt.getServices().size() == 0){
            this.refreshServices(null);
            while(my_state == ManagerState.SERVICE_DISCOVERY){
                try {
                    Thread.sleep(1);
                }catch(Exception e){

                }
            }
        }

        BluetoothGattCharacteristic c_to_read = null ;
        for(BluetoothGattService s : this.gatt.getServices()){
            for(UUID know_service : this.services){
                if(s.getUuid().equals(know_service)){
                    for(BluetoothGattCharacteristic c : s.getCharacteristics()){
                        if(c.getUuid().equals(serviceUuid)){
                            c_to_read = c ;
                            this.gatt.setCharacteristicNotification(c, true);
                            readUUIDs.put(serviceUuid, callBack);
                            return true ;
                        }
                    }

                }

            }

        }
        return false ;

    }

    @Override
    public boolean write(UUID serviceUuid, byte[] data, int synchronous) {

        if(this.gatt.getServices().size() == 0){
            this.refreshServices(null);
            while(my_state == ManagerState.SERVICE_DISCOVERY){
                try {
                    Thread.sleep(1);
                }catch(Exception e){

                }
            }
        }
        if(my_state != ManagerState.IDLE) return false ;
        for(BluetoothGattService s : this.gatt.getServices()){
            for(UUID know_service : this.services){
                if(s.getUuid().equals(know_service)){
                    for(BluetoothGattCharacteristic c : s.getCharacteristics()){
                        if(c.getUuid().equals(serviceUuid)){
                            this.my_state = ManagerState.WRITING ;
                            c.setValue(data);
                            if(!this.gatt.writeCharacteristic(c)){
                                this.my_state = ManagerState.IDLE ;
                                return false ;
                            }else{
                                return true ;
                            }
                        }
                    }
                }
            }
        }
        return false ;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED && this.my_state == ManagerState.SERVICE_DISCOVERY) {
            if(!gatt.discoverServices()){
                this.my_state = ManagerState.IDLE ;
                this.device_update_callback.onUpdateDeviceDescription(this, null);
            }else{
                gatt.readRemoteRssi();
            }
        }else if(status != BluetoothGatt.GATT_SUCCESS && this.my_state == ManagerState.SERVICE_DISCOVERY){
            this.my_state = ManagerState.IDLE ;
            this.device_update_callback.onUpdateDeviceDescription(this, null);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            for (BluetoothGattService s : gatt.getServices()) {
                this.services.add(s.getUuid());
            }
            this.my_state = ManagerState.IDLE ;
            if(this.device_update_callback != null) {
                this.device_update_callback.onUpdateDeviceDescription(this, this.services);
            }
        }else{
            this.my_state = ManagerState.IDLE ;
            if(this.device_update_callback != null) {
                this.device_update_callback.onUpdateDeviceDescription(this, null);
            }
        }
    }

   public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
       if(status == BluetoothGatt.GATT_SUCCESS && this.my_state == ManagerState.WRITING){
           this.my_state = ManagerState.IDLE;
       }
   }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){
        if(status == BluetoothGatt.GATT_SUCCESS && this.my_state == ManagerState.READING){
            this.my_state = ManagerState.IDLE;
        }else{
            this.my_state = ManagerState.IDLE;
        }
    }

    public void onReadRemoteRssi (BluetoothGatt gatt, int rssi, int status){
        if(status == BluetoothGatt.GATT_SUCCESS && this.my_state == ManagerState.GET_RSSI){ //TODO: handeld proper RSSI state
            this.rssi = rssi ;
            this.my_state = ManagerState.IDLE;
            this.device_update_callback.onUpdateDeviceRssi(this, rssi);
        }else{
            this.my_state = ManagerState.IDLE;
        }
    }


    public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        if(this.readUUIDs.get(characteristic.getUuid()) != null){
            this.readUUIDs.get(characteristic.getUuid()).onDataRead(characteristic.getValue());
        }
    }

}
