package com.boscloner.app.boscloner.cloner.cmd;

/**
 * Created by jpiat on 10/19/15.
 */
public enum CommandParserState {
    IDLE,
    PREFIX,
    CLONE_CMD,
    SCAN_CMD,
    CARD_ID,
    SUFFIX,
    DONE,
    ERROR
}
