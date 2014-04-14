package com.cwport.sentencer.data;

/**
 * Created by isayev on 02.02.14.
 */
public class DataProviderFactory {
    public static DataProvider getInstance(ProviderType providerType) {
        DataProvider dp;
        switch (providerType) {
            default:
                // FILE provider by default
                dp = new FileDataProvider();
        }
        return dp;
    }
}
