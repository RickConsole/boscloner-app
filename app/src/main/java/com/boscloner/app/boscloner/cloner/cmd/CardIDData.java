package com.boscloner.app.boscloner.cloner.cmd;

import com.boscloner.app.boscloner.cloner.cards.CardId;

/**
 * Created by jpiat on 11/24/15.
 */
public class CardIDData extends CommandData {

    private CardId id ;

    public CardIDData(String id) {
        this.id = new CardId(id);
    }

    public CardIDData(CardId id) {
        this.id = id;
    }

    public CardId getId(){
        return id ;
    }

    public String toString(){
        return this.id.toString();
    }

}
