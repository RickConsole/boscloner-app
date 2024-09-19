package com.boscloner.app.boscloner.cloner;

import com.boscloner.app.boscloner.cloner.cmd.ClonerCommand;

/**
 * Created by jpiat on 10/16/15.
 */
public interface IClonerObserver {

    public void onClonerCommand(ClonerCommand cmd);
    public void onClonerDisconnect();

}
