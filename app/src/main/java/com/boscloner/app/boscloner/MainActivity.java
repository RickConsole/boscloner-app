package com.boscloner.app.boscloner;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.boscloner.app.boscloner.bluetooth.BleManager;
import com.boscloner.app.boscloner.bluetooth.BluetoothManager;
import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;
import com.boscloner.app.boscloner.bluetooth.IUpdatedDescriptionCallBack;
import com.boscloner.app.boscloner.cloner.AbstractBluetoothCloner;
import com.boscloner.app.boscloner.cloner.AuthorizedCloners;
import com.boscloner.app.boscloner.cloner.BosCloner;
import com.boscloner.app.boscloner.cloner.ClonerFactory;
import com.boscloner.app.boscloner.cloner.ConnectedClonerContainner;
import com.boscloner.app.boscloner.cloner.IClonerObserver;
import com.boscloner.app.boscloner.cloner.cmd.ClonerCommand;
import com.boscloner.app.boscloner.cloner.cmd.CommandType;
import com.boscloner.app.boscloner.listeners.AvailableBluetoothDeviceList;
import com.example.jpiat.boscloner.R;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener, AdapterView.OnClickListener, IUpdatedDescriptionCallBack, IClonerObserver {

    private static final int REQUEST_ENABLE_BT = 1;
    private Menu menu;
    private static long clone_vibrate_pattern [] = {100, 100, 100, 100, 100, 100} ;
    private static long disconnect_vibrate_pattern [] = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50} ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Spinner devList = (Spinner) findViewById(R.id.devices_spinner);
        devList.setOnItemSelectedListener(this);
        Switch enableSwitch = (Switch) findViewById(R.id.cloneSwitch);
        enableSwitch.setOnCheckedChangeListener(this);
        Button connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(this);
        if (ConnectedClonerContainner.getInstance().getCloner() != null && ConnectedClonerContainner.getInstance().getCloner().connected()) {
            ConnectedClonerContainner.getInstance().getCloner().addObserver(this);
            enableSwitch.setEnabled(ConnectedClonerContainner.getInstance().getCloner().getAutoClone());
        } else {
            updateDevicesList();
            enableSwitch.setEnabled(false);
        }
        if(this.getSupportActionBar() != null) {
            this.getSupportActionBar().setDisplayShowHomeEnabled(true);
            this.getSupportActionBar().setIcon(R.mipmap.boscloner_icon);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (ConnectedClonerContainner.getInstance().getCloner() == null || !ConnectedClonerContainner.getInstance().getCloner().connected()) {
            setDisconnectedState();
        } else {
            setConnectedState();
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void setConnectedState() {
        Button but = (Button) findViewById(R.id.connect_button);
        ((TextView) findViewById(R.id.textView)).setText("");
        for (int i = 0; i < menu.size(); i++) { // disable menu options, if no cloner is connected
            MenuItem m = menu.getItem(i);
            m.setEnabled(true);
        }
        if (findViewById(R.id.action_refresh) != null) {
            findViewById(R.id.action_refresh).setEnabled(false);
        }
        but.setText(getResources().getString(R.string.disconnect));
        findViewById(R.id.cloneSwitch).setEnabled(true);
    }

    private void setDisconnectedState() {
        Button but = (Button) findViewById(R.id.connect_button);
        but.setText(getResources().getString(R.string.connect));
        for (int i = 0; i < this.menu.size(); i++) { // disable menu options, if no cloner is connected
            MenuItem m = this.menu.getItem(i);
            if(m.getItemId() == R.id.action_refresh){
                m.setEnabled(true);
            }else {
                m.setEnabled(false);
            }
        }
        findViewById(R.id.cloneSwitch).setEnabled(false);
        updateDevicesList();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                //TODO: add response handler (bluetooth not enabled)
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateDevicesList();
                return true;
            case R.id.action_status:
                Intent s = new Intent(getApplicationContext(), StatusActivity.class);
                startActivity(s);
                return true;
            case R.id.action_history:
                Intent h = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(h);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    protected void updateDevicesList() {
        Set<BluetoothDevice> slaves = this.getBluetoothPaired();
        AvailableBluetoothDeviceList.getInstance().clear();
        for (BluetoothDevice b : slaves) {
            if (AuthorizedCloners.isAuthorized(b.getAddress())) {
                if (b.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                    AvailableBluetoothDeviceList.getInstance().addItem(new BleManager(this, b));
                } else {
                    AvailableBluetoothDeviceList.getInstance().addItem(new BluetoothManager(this, b));
                }
            }
        }
        Spinner devList = (Spinner) findViewById(R.id.devices_spinner);
        AvailableBluetoothDeviceList.getInstance().setContext(this);
        devList.setAdapter(AvailableBluetoothDeviceList.getInstance());
        if( AvailableBluetoothDeviceList.getInstance().getCount() == 0){
            Button but = (Button) findViewById(R.id.connect_button);
            but.setEnabled(false);
        }else{
            Button but = (Button) findViewById(R.id.connect_button);
            but.setEnabled(true);
        }
    }

    protected Set<BluetoothDevice> getBluetoothPaired() {
        Set<BluetoothDevice> pairedDevices;
        BluetoothAdapter BA;
        BA = BluetoothAdapter.getDefaultAdapter();
        if (BA != null && !BA.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        pairedDevices = BA.getBondedDevices();
        return pairedDevices;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.cloneSwitch) {
            if (isChecked) {
                AbstractBluetoothCloner activeCloner = ConnectedClonerContainner.getInstance().getCloner();
                if (activeCloner != null) {
                    activeCloner.setAutoClone(true);
                }
            } else {
                AbstractBluetoothCloner activeCloner = ConnectedClonerContainner.getInstance().getCloner();
                if (activeCloner != null) {
                    activeCloner.setAutoClone(false);
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void refreshTerminalViewWithDeviceInformations(IBluetoothManager dev) {
        if (dev != null) {
            if (!dev.getServices(this)) {
                TextView terminalView = (TextView) findViewById(R.id.textView);
                terminalView.setText(getResources().getString(R.string.error));
            } else {
                TextView terminalView = (TextView) findViewById(R.id.textView);
                terminalView.setText(getResources().getString(R.string.loading));
            }
        } else {
            TextView terminalView = (TextView) findViewById(R.id.textView);
            terminalView.setText(getResources().getString(R.string.error));
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (AvailableBluetoothDeviceList.getInstance().getSelectedItem() != null) {
            AvailableBluetoothDeviceList.getInstance().getSelectedItem().disconnect();
        }
        IBluetoothManager dev = (IBluetoothManager) AvailableBluetoothDeviceList.getInstance().getItem(position);
        AvailableBluetoothDeviceList.getInstance().setSelectedItem(position);
        refreshTerminalViewWithDeviceInformations(dev);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        TextView desc = (TextView) this.findViewById(R.id.textView);
        desc.setText(getResources().getString(R.string.select_device));
    }

    @Override
    public void onClick(View v) {
        getResources().getString(R.string.connect);
        if (v.getId() == R.id.connect_button) {
            Button but = (Button) v;
            if (but.getText().equals(getResources().getString(R.string.connect))) {
                List<UUID> uuids = AvailableBluetoothDeviceList.getInstance().getDeviceUUID(AvailableBluetoothDeviceList.getInstance().getSelectedItem());
                ConnectedClonerContainner activeClonerContainer = ConnectedClonerContainner.getInstance();
                if(AvailableBluetoothDeviceList.getInstance().getSelectedItem() == null) return ;
                if (uuids == null || uuids.size() == 0) { // trying to connect with default cloner profile
                    activeClonerContainer.setCloner(new BosCloner(AvailableBluetoothDeviceList.getInstance().getSelectedItem()));
                }
                for (UUID u : uuids) {
                    AbstractBluetoothCloner activeCloner = ClonerFactory.fromUuid(u, AvailableBluetoothDeviceList.getInstance().getSelectedItem());
                    if (activeCloner != null) {
                        activeClonerContainer.setCloner(activeCloner);
                        break;
                    }
                }
                if (activeClonerContainer.getCloner() != null) {
                    if (!activeClonerContainer.getCloner().connect()){
                        //Failed to connect, trigger a pop-up
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User clicked OK button
                            }
                        });
                        builder.setMessage("Failed to connect to device");
                        builder.setTitle("Failed to connect to device");
                        AlertDialog dialog = builder.create();
                        dialog.show();
                        return;
                    }
                    activeClonerContainer.getCloner().addObserver(this);
                    setConnectedState();
                }
            } else {
                ConnectedClonerContainner activeClonerContainer = ConnectedClonerContainner.getInstance();
                activeClonerContainer.getCloner().close();
                activeClonerContainer.setCloner(null);
                AvailableBluetoothDeviceList.getInstance().getSelectedItem().disconnect();
                //setDisconnectedState();
            }
        }
    }

    /*
    Update the text view field in the UI thread. Good workaround for updating outside UI thread
     */
    public void onUpdateDeviceDescription(final IBluetoothManager dev, final List<UUID> desc) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView terminalView = (TextView) findViewById(R.id.textView);
                String deviceDescriptor = new String();
                deviceDescriptor += "Device address is :" + dev.getDevice().getAddress() + "\n";
                if (desc == null) {
                    deviceDescriptor += getResources().getString(R.string.error);

                } else {
                    for (UUID uid : desc) {
                        AvailableBluetoothDeviceList.getInstance().addUUID(dev, uid);
                        deviceDescriptor += "\t" + uid.toString() + "\n";
                    }
                }
                terminalView.setText(deviceDescriptor);
            }
        });
    }

    @Override
    public void onUpdateDeviceRssi(IBluetoothManager dev, final int rssi) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView rssiView = (TextView) findViewById(R.id.rssi_text);
                rssiView.setText("RSSI : " + String.valueOf(rssi));
            }
        });
    }

    @Override
    public void onClonerCommand(final ClonerCommand cmd) {
        runOnUiThread(new Runnable() {
            public void run() {
                TextView terminalView = (TextView) findViewById(R.id.textView);
                terminalView.setText(terminalView.getText() + new String("\n" + cmd.getType().toString() + " : " + cmd.getData().toString()));
            }
        });
        if(cmd.getType() == CommandType.CLONE || cmd.getType() == CommandType.SCAN) {
            Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(clone_vibrate_pattern, -1);

            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mp = MediaPlayer.create(this, notification);
            mp.start(); // should play the notification tone ...
        }
    }

    @Override
    public void onClonerDisconnect() {
        runOnUiThread(new Runnable() {
            public void run() {
                setDisconnectedState();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.disconnected_msg);
                builder.setTitle(R.string.disconnected_msg);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
                AlertDialog dialog = builder.create();
                try{
                dialog.show();
                }catch(Exception e){
                    //should not happen, but in case the activity is not running anymore.
                }

            }
        });
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(disconnect_vibrate_pattern, -1);

    }
}