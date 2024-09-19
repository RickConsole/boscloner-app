package com.boscloner.app.boscloner.cloner;

/**
 * Created by jpiat on 12/17/15.
 */
public class ConnectedClonerContainner {

    private static ConnectedClonerContainner single = null ;
    private AbstractBluetoothCloner cloner ;

    private ConnectedClonerContainner(){
        cloner = null ;
    }

    public static ConnectedClonerContainner getInstance(){
        if(single == null){
            single = new ConnectedClonerContainner();
        }
        return single ;
    }


    public void setCloner(AbstractBluetoothCloner cloner){
         this.cloner = cloner ;
    }

    public AbstractBluetoothCloner getCloner(){
        return this.cloner ;
    }

}
