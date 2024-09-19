package com.boscloner.app.boscloner.cloner.cards;

import java.security.Timestamp;

/**
 * Created by jpiat on 10/13/15.
 */
public abstract class AbstractCard {

    protected CardId cardId ;
    protected int data_size ;
    protected byte [] data ;
    protected boolean cloned ;
    protected Timestamp ts ;

    public AbstractCard(){
        cardId = null ;
        data_size = 0 ;
        cloned = false ;
    }

    public AbstractCard(CardId id){
        cardId = id ;
        data_size = 0 ;
        cloned = false ;
    }

    public CardId getCardId(){
        return cardId ;
    }

    public void setCardId(CardId id){
        this.cardId = id ;
    }

    public byte[] getCardData(){
        return this.data ;
    }

    public void setCardData(byte [] data){
       this.data = data ;
    }

    public boolean isCloned(){
        return this.cloned ;
    }

    public void setCLoned(){
        this.cloned = true ;
    }
}
