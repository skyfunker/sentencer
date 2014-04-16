package com.cwport.sentencer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by isayev on 16.04.2014.
 */
public class CommonUtils {

    public static boolean isConnectedOrConnectingToNetwork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null)
            return false;

        NetworkInfo[] infos = cm.getAllNetworkInfo();

        for (NetworkInfo info : infos)
            if (info.isConnectedOrConnecting())
                return true;

        return false;
    }
}
