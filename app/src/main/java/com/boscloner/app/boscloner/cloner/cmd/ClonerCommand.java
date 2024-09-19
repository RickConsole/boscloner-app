package com.boscloner.app.boscloner.cloner.cmd;

/**
 * Created by jpiat on 10/16/15.
 */
public class ClonerCommand {

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }

    private CommandType type ;

    public CommandData getData() {
        return data;
    }

    public void setData(CommandData data) {
        this.data = data;
    }

    public String toRawString(){
        String cmd = new String();
        cmd = cmd + CommandPrefix.VALUE + this.getType().toString() ;
        //cmd = cmd + "," ;
        if(this.getData() != null){
            cmd = cmd + "," ;
            cmd = cmd + this.getData().toString();
        }
        cmd = cmd + CommandSuffix.VALUE ;
        return cmd ;
    }

    private CommandData data ;



}
