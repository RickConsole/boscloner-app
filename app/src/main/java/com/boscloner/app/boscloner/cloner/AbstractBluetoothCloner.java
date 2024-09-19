package com.boscloner.app.boscloner.cloner;

import android.annotation.TargetApi;
import android.os.Build;

import com.boscloner.app.boscloner.bluetooth.IBluetoothManager;
import com.boscloner.app.boscloner.bluetooth.IDataReceivedCallBack;
import com.boscloner.app.boscloner.cloner.cards.AbstractCard;
import com.boscloner.app.boscloner.cloner.cmd.ClonerCommand;
import com.boscloner.app.boscloner.cloner.cmd.CommandParser;
import com.boscloner.app.boscloner.cloner.cmd.CommandType;
import com.boscloner.app.boscloner.cloner.cmd.IPaserObserver;
import com.boscloner.app.boscloner.cloner.cmd.ParserException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by jpiat on 10/13/15.
 */
public abstract class AbstractBluetoothCloner implements IDataReceivedCallBack, IPaserObserver {


    protected IBluetoothManager bluetooth_manager ;
    protected List<IClonerObserver> observers ;
    protected CommandParser parser ;

    protected List<AbstractCard> scannedCards ;
    private static final int MAX_CARDS = 20 ;

    public AbstractBluetoothCloner(IBluetoothManager dev){
        this.bluetooth_manager = dev ;
        this.scannedCards = new ArrayList<AbstractCard>();
        this.observers = new ArrayList<IClonerObserver>();
    }

    public void addObserver(IClonerObserver obs){
        this.observers.add(obs);
    }

    protected abstract UUID getRxUUID();
    protected abstract UUID getTxUUID();
    protected abstract UUID getClonerUUID();
    public abstract byte[] getPin();
    private boolean auto_clone = false ;

    public void setAutoClone(boolean set){
        if(set){
            //TODO: send command to cloner to enable
            //this.getBluetoothManager().write(this.getTxUUID(), );
            ClonerCommand enableCloneCommand = new ClonerCommand();
            enableCloneCommand.setType(CommandType.ENABLE_CLONE);
            this.sendCommand(enableCloneCommand);
        }else{
            ClonerCommand disableCloneCommand = new ClonerCommand();
            disableCloneCommand.setType(CommandType.DISABLE_CLONE);
            this.sendCommand(disableCloneCommand);
        }
        this.auto_clone = set ;
    }


    public boolean getAutoClone(){
        return this.auto_clone ;
    }
    public boolean sendCommand(ClonerCommand cmd){
        if(this.bluetooth_manager.isConnected()){
            this.write(cmd.toRawString().getBytes());
            return true ;
        }
        return false ;
    }


    public void addScannedCard(AbstractCard newCard){ //TODO: should move to a sorted set, sorted based on the timestamp
        if(this.scannedCards.size() >= MAX_CARDS){
            //Removing oldest card in the list
            this.scannedCards = this.scannedCards.subList(1, this.scannedCards.size() - 1);
        }
        this.scannedCards.add(newCard);
    }

    public abstract String getClonerId();
    public AbstractCard getLatestClonedCard(){
        AbstractCard result = null;
        if(this.scannedCards.size() > 0) {
            for (AbstractCard c : this.scannedCards) { //TODO: sould iterate on a revert list
                if (c.isCloned()) {
                    result = c;
                }
            }
            return result ;
        }
        return null ;
    }

    public AbstractCard getLatestScannedCard(){
        if(this.scannedCards.size() > 0){ //TODO: should order cards based on timestamp
            return this.scannedCards.get(this.scannedCards.size()-1);
        }
        return null ;
    }
    public List<AbstractCard> getCardHistory(){
        return this.scannedCards ;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private byte[] read() {
        return this.bluetooth_manager.read(this.getRxUUID());
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private boolean write(byte[] data) {
        return this.bluetooth_manager.write(this.getTxUUID(), data, 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public boolean connect(){
        if(!this.bluetooth_manager.connect(this.getRxUUID(), this.getTxUUID())){
            return false ;
        }
        this.parser = new CommandParser();
        this.addObserver(ClonerHistory.getInstance());
        this.parser.addObserver(this);
        this.bluetooth_manager.enableAsyncRead(this.getRxUUID(), this);
        ClonerCommand syncCommand = new ClonerCommand();
        syncCommand.setType(CommandType.SYNC);
        this.sendCommand(syncCommand);
        //this.bluetooth_manager.write(this.getTxUUID(), hello.getBytes(), 0);
        return true ;
    }


    public boolean connected(){
        return this.bluetooth_manager.isConnected();
    }
    public void close(){
        this.bluetooth_manager.disconnect();
    }

    public IBluetoothManager getBluetoothManager(){
        return this.bluetooth_manager;
    }

    public void commandParsed(ClonerCommand cmd) {
        for(IClonerObserver obs : this.observers){
            obs.onClonerCommand(cmd);
        }
    }

    public void onDataRead(byte[] data) {
        try {
            this.parser.pushTokens(data);
        }catch(ParserException e) {

        }

    }

    public void onDisconnect(){
        for(IClonerObserver c : this.observers){
            c.onClonerDisconnect();
        }
    }

}
