package com.cwport.sentencer.data;

/**
 * Created by isayev on 02.02.14.
 */
public class DataProviderFactory {
    public static DataProvider getInstance(SourceType sourceType) {
        DataProvider dp;
        if(sourceType == SourceType.INTERNAL) {
            dp = new InternalDataProvider();
        } else {
            dp = new AssetDataProvider();
        }
        return dp;
    }
}
