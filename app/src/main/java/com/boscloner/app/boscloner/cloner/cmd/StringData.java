package com.boscloner.app.boscloner.cloner.cmd;

import com.boscloner.app.boscloner.cloner.cards.CardId;

/**
 * Created by jpiat on 11/24/15.
 */
public class StringData extends CommandData {

    private String string ;

    public StringData(String str) {
        this.string = str;
    }


    public String toString(){
        return this.string ;
    }

}
