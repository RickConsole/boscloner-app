package com.boscloner.app.boscloner.cloner.cards;

import java.util.Formatter;

/**
 * Created by jpiat on 10/13/15.
 */
public class CardId {

    private byte[] id ;

    public CardId(byte [] id){
        this.id = id.clone() ;
    }

    public CardId(String id){
        this.id =  hexStringToByteArray(id);
    }

    public static byte[] hexStringToByteArray(String s) {
        s = s.replace(":", "");//removing separator if any
        if(s.length()%2 != 0){ //for even number of hex, add a zero in front.
            s = "0"+s ;
        }
        int len = s.length();
        int arrayIndex = 0 ;
        byte[] data = new byte[(len+1) / 2];

        for (int i = 0 ; i < len; ) {
                data[arrayIndex] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                        + Character.digit(s.charAt(i + 1), 16));
                arrayIndex += 1 ;
                i +=2 ;
        }
        return data;
    }

    public String toString(){
        String idString ;
        Formatter formatter = new Formatter();
        for (byte b : id) {
            if(b == id[id.length - 1]){
                formatter.format("%02x", b);
            }else {
                formatter.format("%02x:", b);
            }
        }
        idString = formatter.toString() ;
        formatter.close();
        return idString ;
    }

}
