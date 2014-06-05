package com.cwport.sentencer.data;

import android.content.Context;

import com.cwport.sentencer.model.Lesson;

import java.util.ArrayList;

/**
 * Created by isayev on 10.03.14.
 */
public class DataManager {
    private DataProvider internalDataProvider;
    private DataProvider assetDataProvider;

    public DataManager() {
    }

    public DataProvider getDataProvider(SourceType sourceType, Context context) {
        if(sourceType == SourceType.INTERNAL) {
            if (internalDataProvider == null) {
                internalDataProvider = DataProviderFactory.getInstance(SourceType.INTERNAL);
                internalDataProvider.setContext(context);
            }
            return internalDataProvider;
        } else {
            if (assetDataProvider == null) {
                assetDataProvider = DataProviderFactory.getInstance(SourceType.ASSET);
                assetDataProvider.setContext(context);
            }
            return assetDataProvider;
        }
    }
}
