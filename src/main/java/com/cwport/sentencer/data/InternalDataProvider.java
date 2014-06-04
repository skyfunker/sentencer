package com.cwport.sentencer.data;

import android.content.Context;
import android.util.Log;

import com.cwport.sentencer.model.Card;
import com.cwport.sentencer.model.Lesson;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by isayev on 02.02.14.
 */
public class InternalDataProvider implements DataProvider {
    public static final String TAG = InternalDataProvider.class.getSimpleName();

    public static final String ERROR_CANNOT_READ_LESSON_FILE = "Cannot read lesson file";
    public static final String ERROR_LESSON_NOT_EXIST = "Lesson doesn't exist";

    private String lessonFolder;
    private ArrayList<Lesson> lessons = null;
    private String[] lessonTitles = new String[]{};

    private Context context;

    public InternalDataProvider() {}

    @Override
    public void setContext(Context appContext) {
        this.context =  appContext;
    }

    @Override
    public String[] getLessonTitles() throws DataException {
        if(lessons == null) {
            readUserLessons();
        }
        if(lessonTitles.length != lessons.size()) {
            lessonTitles = new String[lessons.size()];
            for (int i = 0; i < lessons.size(); i++) {
                lessonTitles[i] = lessons.get(i).getTitle();
            }
        }
        return lessonTitles;
    }

    @Override
    public ArrayList<Lesson> getLessons() throws DataException {
        if(lessons == null) {
            readUserLessons();
        }
        return lessons;
    }

    @Override
    public Lesson getLesson(int index) throws DataException {
        Lesson lesson;
        if(lessons == null) {
            readUserLessons();
        }
        if(index > (lessons.size() - 1)) {
            throw new DataException(ERROR_LESSON_NOT_EXIST);
        }
        lesson = lessons.get(index);
        return lesson;
    }

    private void readUserLessons() throws DataException {
        if(lessons != null) { lessons.clear(); }
        else { lessons = new ArrayList<Lesson>(); }

        File filesDir = context.getFilesDir();
        for(String fileName : filesDir.list()) {
            Log.i(TAG, "Internal file found: " + fileName);
            Lesson lesson = readUserLesson(fileName);
            lessons.add(lesson);
        }
    }

    private Lesson readUserLesson(String fileName) throws DataException {
        Lesson lesson = new Lesson();
        ArrayList<Card> cards = new ArrayList<Card>();
        try {
            // open file
            FileInputStream fileInputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            CSVReader reader = new CSVReader(inputStreamReader, ';', '\"', 0);
            String [] nextLine;
            if((nextLine = reader.readNext()) != null) {
                // read meta info from the first line
                lesson = new Lesson(nextLine[0], nextLine[1], nextLine[2],
                        nextLine[3], nextLine[4], Integer.parseInt(nextLine[5]));
                lesson.setId(DataHelper.md5(lesson.getFilename() + lesson.getCardCount()));
                lesson.setSourceType(SourceType.INTERNAL);
                while ((nextLine = reader.readNext()) != null) {
                    String faceText = nextLine[0].trim();
                    String backText = nextLine[1].trim();
                    Card c = new Card(DataHelper.md5(faceText), faceText, backText, false);
                    cards.add(c);
                }
            }
            lesson.setCards(cards);
            reader.close();
            fileInputStream.close();
        } catch(UnsupportedEncodingException ex) {
            Log.e(TAG, ex.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, ex);
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, e);
        }
        return lesson;
    }
}
