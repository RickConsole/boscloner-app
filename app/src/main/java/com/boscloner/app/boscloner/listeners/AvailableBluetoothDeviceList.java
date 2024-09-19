package com.boscloner.app.boscloner.listeners;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.jpiat.boscloner.R;
import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jpiat on 10/12/15.
 */
public class AvailableBluetoothDeviceList extends BaseAdapter{

    private List<IBluetoothManager> devicesList;
    private Map<IBluetoothManager, List<UUID>> devicesUUID ;
    private int selectedItem = 0;
    private Context context ;
    private static AvailableBluetoothDeviceList instance = null;


    public static AvailableBluetoothDeviceList getInstance() {
        if (instance == null) {
            instance = new AvailableBluetoothDeviceList();
        }
        return instance;

    }

    private AvailableBluetoothDeviceList() {
        devicesList = new ArrayList<IBluetoothManager>();
        devicesUUID = new HashMap<IBluetoothManager, List<UUID>>();
    }

    public IBluetoothManager getSelectedItem() {
        return (IBluetoothManager) this.getItem(this.selectedItem);
    }

    public void addItem(IBluetoothManager b) {
        if(!devicesList.contains(b)){
            devicesList.add(b);
            devicesUUID.put(b, new ArrayList<UUID>());
        }
    }

    public void addUUID(IBluetoothManager b, UUID uuid) {
        if(! devicesUUID.get(b).contains(uuid)){
            devicesUUID.get(b).add(uuid);
        }
    }

    public List<UUID> getDeviceUUID(IBluetoothManager b){
        return devicesUUID.get(b);
    }

    public void clear() {
        devicesList.clear();
        devicesUUID.clear();
    }

    @Override
    public int getCount() {
        return devicesList.size();
    }

    @Override
    public Object getItem(int position) {
        return devicesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IBluetoothManager currentListData = (IBluetoothManager) getItem(position);
        View view;
        RowHolder holder;
        if(convertView == null) {
            view = LayoutInflater.from(this.context).inflate(R.layout.text_row_layout, parent, false);
            holder = new RowHolder();
            holder.row_text = (TextView)view.findViewById(R.id.row_text);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = (RowHolder)view.getTag();
        }
        holder.row_text.setText(currentListData.getName());
        return view;
    }

    private class RowHolder{
        public TextView row_text ;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setSelectedItem(int item){
        this.selectedItem = item ;
    }
}
