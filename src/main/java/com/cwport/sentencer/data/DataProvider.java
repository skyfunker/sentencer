package com.cwport.sentencer.data;

import android.content.Context;

import com.cwport.sentencer.model.Card;
import com.cwport.sentencer.model.Lesson;

import java.util.ArrayList;

/**
 * Created by isayev on 02.02.14.
 */
public interface DataProvider {
    public ArrayList<Lesson> getLessons() throws DataException;
    public String[] getLessonTitles() throws DataException;
    public Lesson getLesson(int index) throws DataException;
    public void setContext(Context context);
}
