package com.boscloner.app.boscloner;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;
import com.boscloner.app.boscloner.bluetooth.IUpdatedDescriptionCallBack;
import com.boscloner.app.boscloner.listeners.AvailableBluetoothDeviceList;
import com.example.jpiat.boscloner.R;

import java.util.List;
import java.util.UUID;

/**
 * Created by jpiat on 10/12/15.
 */
public class StatusActivity extends AppCompatActivity implements IUpdatedDescriptionCallBack {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        IBluetoothManager dev = AvailableBluetoothDeviceList.getInstance().getSelectedItem();
        TextView statusText = (TextView) findViewById(R.id.status_text);
        statusText.setText(getResources().getString(R.string.loading));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        AvailableBluetoothDeviceList.getInstance().getSelectedItem().getServices(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.status, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                //TODO update status
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onUpdateDeviceDescription(final IBluetoothManager dev, final List<UUID> desc) {
        runOnUiThread(new Runnable() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
            public void run() {
                TextView statusText = (TextView) findViewById(R.id.status_text);
                String deviceDescriptor = new String();
                deviceDescriptor += "Device type is :" ;
                if(dev.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_LE){
                    deviceDescriptor += " BLE";
                }else if(dev.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC){
                    deviceDescriptor += " Classic";
                }else if(dev.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_DUAL){
                    deviceDescriptor += " Dual";
                }else if(dev.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_UNKNOWN){
                    deviceDescriptor += " Unknown";
                }
                deviceDescriptor +="\n";
                deviceDescriptor += "Device address is :" + dev.getDevice().getAddress() + "\n";
                if(desc == null){
                    deviceDescriptor += getResources().getString(R.string.error);

                }else {
                    for (UUID uid : desc) {
                        deviceDescriptor += "\t" + uid.toString() + "\n";
                    }
                }

                statusText.setText(deviceDescriptor);
            }
        });
    }

    @Override
    public void onUpdateDeviceRssi(IBluetoothManager dev, int rssi) {

    }
}
