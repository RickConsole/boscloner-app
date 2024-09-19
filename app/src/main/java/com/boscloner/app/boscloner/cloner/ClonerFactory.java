package com.boscloner.app.boscloner.cloner;

import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jpiat on 10/14/15.
 */
public class ClonerFactory {


    private static final Map<UUID, Class> cloner_map = new HashMap<UUID, Class>(){
        {
            put(BosCloner.BOS_CLONER_RX_UUID, BosCloner.class);
            put(BosCloner.BOS_CLONER_TX_UUID, BosCloner.class);
            put(BleBosCloner.BOS_CLONER_UUID, BleBosCloner.class);
        }
    };

    public static AbstractBluetoothCloner fromUuid(UUID clonerUUID, IBluetoothManager dev){
        Class clonerClass =  cloner_map.get(clonerUUID);
        if(clonerClass != null){
            try {
                Constructor c = clonerClass.getConstructor(IBluetoothManager.class);
                return (AbstractBluetoothCloner) c.newInstance(dev);
            }catch(Exception e){
                return null ;
            }
        }
        return null ;
    }
}
