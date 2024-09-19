package com.boscloner.app.boscloner.cloner.cmd;

/**
 * Created by jpiat on 10/16/15.
 */
public enum CommandType {
    ENABLE_CLONE ("CLONE_EN") ,
    DISABLE_CLONE("CLONE_DIS"),
    CLONE ("CLONE"),
    SCAN ("SCAN"),
    SYNC ("SYNC"),
    STATUS ("STATUS");


    private CommandType(String val){
    }



}
