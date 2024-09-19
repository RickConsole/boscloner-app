package com.boscloner.app.boscloner.cloner.cmd;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jpiat on 10/16/15.
 *
 *
 * example valid command : $CLONE!56:76:87;67;98\n
 **/
public class CommandParser {

    protected List<IPaserObserver> observers ;
    protected CommandParserState state ;
    protected List<Byte> contextBytes ;
    protected ClonerCommand lastCommand ;

    public CommandParser(){
        this.observers = new ArrayList<IPaserObserver>();
        this.state = CommandParserState.IDLE ;
        this.contextBytes = new ArrayList<Byte>();
        lastCommand = new ClonerCommand();
    }

    public void addObserver(IPaserObserver obs){
        this.observers.add(obs);
    }

    private void notifyObservers(ClonerCommand cmd){
        for(IPaserObserver obs : this.observers){
            obs.commandParsed(cmd);
        }
    }

    public boolean pushTokens(byte [] tokens) throws ParserException{
        for(byte b : tokens) {
            this.contextBytes.add(new Byte(b));
            if(b == '\n' || b == '\r'){ // we should now have enough data to start parsing
                return this.detectPrefix();
            }
        }
        return false ;
    }

    private boolean detectPrefix(){
        String prefixString = new String();
        while(this.contextBytes.size() > 0){
            prefixString += String.valueOf((char) this.contextBytes.get(0).byteValue());
            this.contextBytes.remove(0);
            if(prefixString.length() > CommandPrefix.VALUE.length())prefixString = prefixString.substring(1);
            if(prefixString.equals(CommandPrefix.VALUE)){
                break ;
            }
        }
        if(this.contextBytes.size() > 0){
            return this.parseCmdName();
        }
        return false;
    }

    private boolean parseCmdName(){
        String cmdName = new String();
        while(this.contextBytes.size() > 0 && this.contextBytes.get(0) != ',') { //should create a class for cmd type sperator
            cmdName += String.valueOf((char) this.contextBytes.get(0).byteValue());
            this.contextBytes.remove(0);
        }
        if(this.contextBytes.size() > 0){
            this.contextBytes.remove(0); //removing ','
            CommandType cmdType = CommandType.valueOf(cmdName);
            this.lastCommand.setType(cmdType);
            if(cmdType == CommandType.STATUS){
                return this.parseString();
            }else{
                return this.parseCardId();
            }

        }
        return false ;
    }

    private boolean parseCardId(){
        String idString = new String();
        while(this.contextBytes.size() > 0 && this.contextBytes.get(0) != '?') {
            idString += String.valueOf((char) this.contextBytes.get(0).byteValue());
            this.contextBytes.remove(0);
        }
        this.lastCommand.setData(new CardIDData(idString));
        return this.detectSuffix() ;
    }

    private boolean parseString(){
        String idString = new String();
        while(this.contextBytes.size() > 0 && this.contextBytes.get(0) != '?') {
            idString += String.valueOf((char) this.contextBytes.get(0).byteValue());
            this.contextBytes.remove(0);
        }
        this.lastCommand.setData(new StringData(idString));
        return this.detectSuffix() ;
    }

    private boolean detectSuffix(){
        String suffixString = new String();
        while(this.contextBytes.size() > 0){
            suffixString += String.valueOf((char) this.contextBytes.get(0).byteValue());
            this.contextBytes.remove(0);
            if(suffixString.equals(CommandSuffix.VALUE)) {
                break;
            }
        }
        if(suffixString.equals(CommandSuffix.VALUE)) this.notifyObservers(this.lastCommand);
        return true ;
    }

}
