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
public class AssetDataProvider implements DataProvider {
    public static final String TAG = AssetDataProvider.class.getSimpleName();

    public static final String DEFAULT_LESSON_FOLDER = "lessons";
    public static final String DEFAULT_LESSON_INDEX_FILE = "index";
    public static final String ERROR_CANNOT_READ_INDEX = "Cannot read index file from assets.";
    public static final String ERROR_LESSON_NOT_EXIST = "Lesson does not exist.";
    public static final String ERROR_CANNOT_READ_LESSON_FILE = "Cannot read lesson file from assets.";

    private String lessonFolder = DEFAULT_LESSON_FOLDER;
    private String lessonIndex = DEFAULT_LESSON_INDEX_FILE;
    private ArrayList<Lesson> lessons = new ArrayList<Lesson>();
    private String[] lessonTitles = new String[]{};

    private Context context;

    public AssetDataProvider() {}

    @Override
    public void setContext(android.content.Context appContext) {
        this.context =  appContext;
    }

    @Override
    public ArrayList<Lesson> getLessons() throws DataException {
        readLessonIndex();
        return lessons;
    }

    @Override
    public Lesson getLesson(String id) throws DataException {
        Lesson lesson = new Lesson();
        for(Lesson l : lessons) {
            if(id.equals(l.getId())) {
                lesson = l;
                break;
            }
        }
        if(id.equals(lesson.getId())) {
            String data = getLessonData(lesson.getFilename());
            lesson.setCards(parseLessonData(data));
        } else {
            throw new DataException(ERROR_LESSON_NOT_EXIST);
        }
        return lesson;
    }

    private void readLessonIndex() throws DataException {
        String lessonData = "";
        InputStream stream = null;
        lessons = new ArrayList<Lesson>();
        try {
            stream = context.getAssets().open(lessonFolder + "/" + lessonIndex);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            lessonData = new String(buffer);
            if(stream != null) { stream.close(); }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_INDEX, e);
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
                lesson.setSourceType(SourceType.ASSET);
                lesson.setId(DataHelper.md5(lesson.getFilename() + lesson.getCardCount()));
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
        Log.d(TAG, "Parse file: " + lessonFile);
        try {
            stream = context.getAssets().open(lessonFolder + "/" + lessonFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            // stream.close();
            lessonData = new String(buffer);
            if(stream != null) { stream.close(); }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, e);

        }
        return lessonData;
    }

    private ArrayList<Card> parseLessonData(String csv) throws DataException {
        ArrayList<Card> cards = new ArrayList<Card>();
        int line = 0;
        try {
            InputStream stream = new ByteArrayInputStream(csv.getBytes("UTF-8"));
            CSVReader reader = new CSVReader(new InputStreamReader(stream), ';', '\"', 0);
            String [] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                String faceText = nextLine[0].trim();
                String backText = nextLine[1].trim();
                Card c = new Card(DataHelper.md5(faceText), faceText, backText, false);
                cards.add(c);
                line++;
            }
            reader.close();
            stream.close();
        } catch(UnsupportedEncodingException ex) {
            Log.e(TAG, "Line:" + line + "; Error: " + ex.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE, ex);
        } catch(Exception e) {
            Log.e(TAG, "Line:" + line + "; Error: " + e.getMessage());
            throw new DataException(ERROR_CANNOT_READ_LESSON_FILE + "at line " + line, e);
        }
        return cards;
    }
}
