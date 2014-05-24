package com.cwport.sentencer.data;

import android.provider.ContactsContract;

import com.cwport.sentencer.model.Lesson;

import java.util.ArrayList;

/**
 * Created by isayev on 10.03.14.
 */
public class DataManager {
    private static DataManager instance;
    private static DataProvider dataProvider;

    private DataManager(ProviderType providerType) {
        dataProvider = DataProviderFactory.getInstance(providerType);
    }

    public static synchronized DataManager getInstance() {
        if(instance == null) {
            instance = new DataManager(ProviderType.FILE);
        }
        return instance;
    }

    public DataProvider getDataProvider() {
        return dataProvider;
    }

    public String[] getLessonTitles() throws DataException {
        return dataProvider.getLessonTitles();
    }

    public ArrayList<Lesson> getLessons() throws DataException {
        return dataProvider.getLessons();
    }

    public Lesson getLesson(int index) throws DataException {
        return dataProvider.getLesson(index);
    }

}
