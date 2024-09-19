package com.boscloner.app.boscloner.cloner;

import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;

import java.util.UUID;

/**
 * Created by jpiat on 10/13/15.
 */
public class BleBosCloner extends AbstractBluetoothCloner {

    private final byte[] pin = {0, 0, 0, 0, 0, 0};

    private static final String BOS_CLONER_ID = "BOS_CLONER";
    public static final UUID BOS_CLONER_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805F9B34FB");
    public static final UUID BOS_CLONER_UART_CHARACTERISIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805F9B34FB");

    public BleBosCloner(IBluetoothManager dev) {
        super(dev);
    }

    @Override
    protected UUID getRxUUID() {
        return BOS_CLONER_UART_CHARACTERISIC_UUID;
    }

    @Override
    protected UUID getTxUUID() {
        return BOS_CLONER_UART_CHARACTERISIC_UUID;
    }

    @Override
    protected UUID getClonerUUID() {
        return BOS_CLONER_UUID;
    }


    @Override
    public String getClonerId() {
        return BOS_CLONER_ID;
    }

    public byte[] getPin() {
        return this.pin;
    }

}
