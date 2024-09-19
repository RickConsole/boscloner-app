package com.boscloner.app.boscloner.bluetooth;

import java.util.List;
import java.util.UUID;

/**
 * Created by jpiat on 10/14/15.
 */
public interface IUpdatedDescriptionCallBack {
    public void onUpdateDeviceDescription(IBluetoothManager dev, List<UUID> services);
    public void onUpdateDeviceRssi(IBluetoothManager dev, int rssi);
}
