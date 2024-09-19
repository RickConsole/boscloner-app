package com.boscloner.app.boscloner.cloner;

import com.boscloner.app.boscloner.cloner.cards.CardId;
import com.boscloner.app.boscloner.cloner.cmd.CommandType;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jpiat on 12/17/15.
 */
public class ClonerEvent {

    private Date timeStamp ;
    private CommandType type ;
    private CardId id ;


    public CardId getId() {
        return id;
    }
    public void setId(CardId id) {
        this.id = id;
    }

    public CommandType getType(){
        return type ;
    }

    public void setType(CommandType type){
        this.type = type ;
    }

    public ClonerEvent(){
        this.timeStamp = new Date();
    }

    public ClonerEvent(CardId id){
        this.timeStamp = new Date();
        this.id = id ;
    }
    public void setTimeStamp(Date time) {
        this.timeStamp = time;
    }

    public void setTimeStamp(String time) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hha:mm:ss");
        this.timeStamp = simpleDateFormat.parse(time);
    }

    public Date getTimeStamp() {
        return this.timeStamp ;
    }

    public String getTimeStampString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hha:mm:ss");
        String format = simpleDateFormat.format(this.getTimeStamp());
        return format ;
    }

    public String getShortDescription(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hha:mm:ss");
        String format = simpleDateFormat.format(this.getTimeStamp());
        return "Clone : "+id.toString()+"\n"+format;
    }

    public int[] parseH10301() {
        String hexData = id.toString().replace(":", "");

        // Convert hexadecimal to binary string
        String binary = String.format("%40s", Long.toBinaryString(Long.parseLong(hexData, 16)))
                .replace(' ', '0');

        // Extract H10301 data
        String h10301Data = binary.substring(14, 40);

        // Calculate facility code and card number
        int facilityCode = Integer.parseInt(h10301Data.substring(1, 9), 2);
        int cardNumber = Integer.parseInt(h10301Data.substring(9, 25), 2);

        return new int[]{facilityCode, cardNumber};
    }
}
