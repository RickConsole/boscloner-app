package com.boscloner.app.boscloner.cloner;

import java.util.ArrayList;

/**
 * Created by jpiat on 12/15/15.
 */
public class AuthorizedCloners {

    private static final ArrayList<String> authorizedIds =  new ArrayList<String>() {{
        //HC-06 Mask
        add("20:15:x:x:x:x");
        //HC-05 Mask
        add("20:17:x:x:x:x");
        add("98:d3:31:x:x:x");
    }};

    public static boolean isAuthorized(String address){
        for(String authAddr : authorizedIds){
            int nb_byte_to_compare = authAddr.indexOf('x');
            if(nb_byte_to_compare < 0) nb_byte_to_compare = authAddr.length(); //compare full address
            String addressId = address.substring(0, nb_byte_to_compare);
            String authId = authAddr.substring(0, nb_byte_to_compare);
            if(addressId.equals(authId)){
                return true ;
            }
        }
        return false ;
    }
}
