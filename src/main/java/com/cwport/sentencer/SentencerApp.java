package com.cwport.sentencer;

import android.app.Application;

import com.cwport.sentencer.data.DataManager;

/**
 * Created by ais on 05.06.2014.
 */
public class SentencerApp extends Application {
    private DataManager dataManager = new DataManager();

    public DataManager getDataManager() {
        return dataManager;
    }
}
