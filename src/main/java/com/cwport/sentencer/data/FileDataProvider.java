package com.cwport.sentencer.data;

import android.content.Context;
import android.util.Log;

import com.cwport.sentencer.model.Card;
import com.cwport.sentencer.model.Lesson;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Created by isayev on 02.02.14.
 */
public class FileDataProvider implements DataProvider {
    public static final String DEFAULT_LESSON_FOLDER = "lessons";
    public static final String DEFAULT_LESSON_INDEX_FILE = "index";
    public static final String ERROR_CANNOT_READ_INDEX = "Cannot read index file from assets.";
    public static final String ERROR_LESSON_NOT_EXIST = "Lesson does not exist.";
    public static final String ERROR_CANNOT_READ_LESSON_FILE = "Cannot read lesson file from assets.";

    public static final String TAG = "FileDataProvider";
    private String lessonFolder = DEFAULT_LESSON_FOLDER;
    private String lessonIndex = DEFAULT_LESSON_INDEX_FILE;
    private ArrayList<Lesson> lessons = null;
    private String[] lessonTitles = new String[]{};

    private Context context;

    public FileDataProvider() {}

    public void setContext(android.content.Context appContext) {
        this.context =  appContext;
    }

    @Override
    public ArrayList<Lesson> getLessons() throws DataException {
        if(lessons == null) {
            readLessonIndex();
        }
        return lessons;
    }

    @Override
    public Lesson getLesson(int index) throws DataException {
        Lesson lesson;
        if(lessons == null) {
            readLessonIndex();
        }
        if(index > (lessons.size() - 1)) {
            throw new DataException(ERROR_LESSON_NOT_EXIST);
        }
        lesson = lessons.get(index);
        if(lesson.getCards().size() == 0) {
            String data = getLessonData(lesson.getFilename());
            lesson.setCards(parseLessonData(data));
        }
        return lesson;
    }

    private void readLessonIndex() throws DataException {
        String lessonData = "";
        InputStream stream = null;
        try {
            stream = context.getAssets().open(lessonFolder + "/" + lessonIndex);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            // stream.close();
            lessonData = new String(buffer);
            if(stream != null) { stream.close(); }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_INDEX, e);
            // lessonData = e.toString();
        }

        try {
            stream = new ByteArrayInputStream(lessonData.getBytes("UTF-8"));
            // read csv data from lesson index file skipping 1st line
            CSVReader reader = new CSVReader(new InputStreamReader(stream), ';', '\"', 1);
            String [] nextLine;
            if(lessons != null) {
                lessons.clear();
            } else {
                lessons = new ArrayList<Lesson>();
            }
            while ((nextLine = reader.readNext()) != null) {
                Lesson lesson = new Lesson(nextLine[0], nextLine[1], nextLine[2],
                        nextLine[3], nextLine[4], Integer.parseInt(nextLine[5]));
                // Log.d(TAG, lesson.toString());
                lessons.add(lesson);
            }
            reader.close();
            stream.close();
        } catch(UnsupportedEncodingException ex) {
            Log.e(TAG, ex.getMessage());
            throw new DataException(ERROR_CANNOT_READ_INDEX, ex);
        } catch(Exception e) {
            Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_INDEX, e);
        }
    }

    private String getLessonData(String lessonFile) throws DataException {
        String lessonData = "";
        InputStream stream = null;
        try {
            stream = context.getAssets().open(lessonFolder + "/" + lessonFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            // stream.close();
            lessonData = new String(buffer);
            if(stream != null) { stream.close(); }
        } catch (IOException e) {
            // Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, e);

        }
        return lessonData;
    }

    private ArrayList<Card> parseLessonData(String csv) throws DataException {
        ArrayList<Card> cards = new ArrayList<Card>();
        try {
            InputStream stream = new ByteArrayInputStream(csv.getBytes("UTF-8"));
            CSVReader reader = new CSVReader(new InputStreamReader(stream), ';', '\"', 0);
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String faceText = nextLine[0];
                String backText = nextLine[1];
                Card c = new Card("", faceText, backText, false);
                // Log.d(TAG, c.toString());
                cards.add(c);
            }
            reader.close();
            stream.close();
        } catch(UnsupportedEncodingException ex) {
            // Log.e(TAG, ex.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, ex);
        } catch(Exception e) {
            // Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, e);
        }
        return cards;
    }

    @Override
    public String[] getLessonTitles() throws DataException {
        if(lessons == null) {
            readLessonIndex();
        }
        if(lessonTitles.length != lessons.size()) {
            lessonTitles = new String[lessons.size()];
            for (int i = 0; i < lessons.size(); i++) {
                lessonTitles[i] = lessons.get(i).getTitle();
            }
        }
        return lessonTitles;
    }
}
